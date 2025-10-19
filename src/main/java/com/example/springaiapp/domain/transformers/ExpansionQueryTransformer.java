package com.example.springaiapp.domain.transformers;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.util.StringUtils;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class ExpansionQueryTransformer implements QueryTransformer {
    private static final PromptTemplate TEMPLATE = PromptTemplate.builder()
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

                                    Original query: {query}
                                    Expanded query:
                            """)
            .build();

    private final ChatClient chatClient;

    public static ExpansionQueryTransformerBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryTransformerBuilder()
                .chatClient(ChatClient.builder(chatModel)
                        .defaultOptions(OllamaOptions.builder()
                                .temperature(0.0)
                                .topP(0.1)
                                .topK(1)
                                .build())
                        .defaultAdvisors(SimpleLoggerAdvisor.builder()
                                .order(0)
                                .build())
                        .build());
    }

    @Override
    public Query transform(Query query) {
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
