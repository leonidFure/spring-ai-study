package com.example.springaiapp.domain.transformers;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class ExpansionQueryTransformer implements QueryTransformer {
        private static final PromptTemplate TEMPLATE = PromptTemplate.builder()
                        .template("""
                                                Расширь полученный запрос добавлением 3–5 тематически релевантных, специализированных слов в конце.
                                                Стратегия выбора слов:
                                                1. Определи основную тему запроса (техническая, научная, творческая и т.д.).
                                                2. Выбери 3–5 слов (МАКСИМУМ 5), которые:
                                                   - усиливают смысл основного запроса;
                                                   - отражают профессиональный контекст темы;
                                                   - повышают точность и глубину интерпретации;
                                                   - не повторяют уже имеющиеся слова.
                                                3. Используй термины высокого уровня, а не общие фразы.
                                                   Примеры:
                                                   - “code optimization” → добавь profiling, JIT, GC tuning, performance analysis
                                                   - “marketing strategy” → добавь segmentation, targeting, ROI, campaign analytics
                                                4. Формат:
                                                   Расширенные слова добавляй в конец запроса через запятую.
                                                   Сам запрос НЕ МЕНЯЙ,
                                                   Пример:
                                                   Input: Explain Java garbage collection
                                                   Output: Explain Java garbage collection, memory management, heap, GC tuning, JVM performance

                                                Инструкция по приоритету выбора:
                                                1. Выбери слова из профессиональной области темы.
                                                2. Если контекст неясен — добавь нейтральные уточняющие (например: principles, architecture, implementation, optimization).
                                                3. Избегай эмоционально окрашенных или оценочных слов.

                                                Query: {query}
                                                Expansion query:
                                        """)
                        .build();

        private final ChatClient chatClient;

        public static ExpansionQueryTransformerBuilder builder(ChatModel chatModel) {
                return new ExpansionQueryTransformerBuilder()
                                .chatClient(ChatClient.builder(chatModel)
                                                .defaultOptions(OllamaOptions.builder()
                                                                // тут желательно использовать температуру 0.0, т.к. мы
                                                                // хотим получить детерминированный ответ при преобразовании запроса
                                                                .temperature(0.0)
                                                                .topP(0.1)
                                                                .topK(1)
                                                                .build())
                                                // .defaultAdvisors(SimpleLoggerAdvisor.builder()
                                                // .order(0)
                                                // .build())
                                                .build());
        }

        @Override
        public @NonNull Query transform(@NonNull Query query) {
                var transformedQueryText = this.chatClient.prompt()
                                .user(TEMPLATE.render(Map.of("query", query.text())))
                                .call()
                                .content();

                if (!StringUtils.hasText(transformedQueryText)) {
                        return query;
                }

                return query.mutate().text(transformedQueryText).build();
        }
}
