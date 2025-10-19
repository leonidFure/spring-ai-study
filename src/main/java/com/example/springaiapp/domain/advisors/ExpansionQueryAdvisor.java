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
public class ExpansionQueryAdvisor implements BaseAdvisor {
    public static final String ORIGINAL_QUESTION = "original_question";
    public static final String ENRICHED_QUESTION = "enriched_question";
    public static final String EXPANSION_RATIO = "expansion_ratio";
    private final ChatClient chatClient;
    @Getter
    private final int order;

    private static final PromptTemplate template = PromptTemplate.builder()
            .template(
                    """
                            Замени во всём тексте все обращения ко второму лицу («ты», «тебе», «тебя», «твой», «твоя», «твоё», «твои», «тобой», «о тебе» и т. п.) на имя «Лёня» с соответствующими изменениями по падежам.
                            Сохрани грамматическую правильность и естественность текста после замены.
                            Если обращений нет, оставь запрос без изменений.
                            Не заменяй имена и фамилии в тексте.
                            Примеры:
                            «Ты молодец!» → Лёня молодец!»
                            «Я видел тебя вчера» → «Я видел Лёню вчера»
                            «Тебе нужно отдохнуть» → Лёне нужно отдохнуть»
                            «Это твоя книга» → «Это книга Лёни»

                            Question: {question}
                            Expanded query:
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
    public ChatClientRequest before(@NonNull ChatClientRequest chatClientRequest, @NonNull AdvisorChain advisorChain) {
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
