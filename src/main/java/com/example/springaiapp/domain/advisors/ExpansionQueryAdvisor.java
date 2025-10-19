package com.example.springaiapp.domain.advisors;

import java.util.Map;
import java.util.Objects;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.lang.NonNull;

import lombok.Builder;
import lombok.Getter;

@Builder
@Deprecated(since = "нет необходимости писать свой advisor для expansion query, т.к. есть ExpansionQueryTransformer")
public class ExpansionQueryAdvisor implements BaseAdvisor {
        public static final String ORIGINAL_QUESTION = "original_question";
        public static final String ENRICHED_QUESTION = "enriched_question";
        public static final String EXPANSION_RATIO = "expansion_ratio";
        private final ChatClient chatClient;
        @Getter
        private final int order;

        private static final PromptTemplate template = PromptTemplate.builder()
                        .template("""
                                        Расширь полученный промпт добавлением 3–5 тематически релевантных, специализированных слов в конце.
                                        Стратегия выбора слов:
                                        1. Определи основную тему промпта (техническая, научная, творческая и т.д.).
                                        2. Выбери 3–5 слов, которые:
                                           - усиливают смысл основного запроса;
                                           - отражают профессиональный контекст темы;
                                           - повышают точность и глубину интерпретации;
                                           - не повторяют уже имеющиеся слова.
                                        3. Используй термины высокого уровня, а не общие фразы.
                                           Примеры:
                                           - “code optimization” → добавь profiling, JIT, GC tuning, performance analysis
                                           - “marketing strategy” → добавь segmentation, targeting, ROI, campaign analytics
                                        4. Формат:
                                           Расширенные слова добавляй в конец промпта через запятую.
                                           Пример:
                                           Input: Explain Java garbage collection
                                           Output: Explain Java garbage collection, memory management, heap, GC tuning, JVM performance

                                        Инструкция по приоритету выбора:
                                        1. Выбери слова из профессиональной области темы.
                                        2. Если контекст неясен — добавь нейтральные уточняющие (например: principles, architecture, implementation, optimization).
                                        3. Избегай эмоционально окрашенных или оценочных слов.

                                        Question: {question}
                                        Expansion query:
                                                                        """)
                        .build();

        public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
                return new ExpansionQueryAdvisorBuilder()
                                .chatClient(ChatClient.builder(chatModel)
                                                .defaultOptions(OllamaOptions.builder()
                                                                .temperature(0.0)
                                                                .topP(0.1)
                                                                .topK(1)
                                                                .build())
                                                .build());
        }

        @NonNull
        @Override
        public ChatClientRequest before(@NonNull ChatClientRequest chatClientRequest,
                        @NonNull AdvisorChain advisorChain) {
                final var query = chatClientRequest.prompt().getUserMessage().getText();
                final var enrichedQuestion = chatClient.prompt()
                                .user(template.render(Map.of("question", query)))
                                .call()
                                .content();

                // просто показатель, что экспаншн отработал
                double ratio = Objects.requireNonNull(enrichedQuestion).length() / (double) query.length();

                return chatClientRequest.mutate()
                                .context(ORIGINAL_QUESTION, query)
                                .context(ENRICHED_QUESTION, Objects.requireNonNull(enrichedQuestion))
                                .context(EXPANSION_RATIO, ratio)
                                .build();
        }

        @NonNull
        @Override
        public ChatClientResponse after(@NonNull ChatClientResponse chatClientResponse,
                        @NonNull AdvisorChain advisorChain) {
                return chatClientResponse;
        }
}
