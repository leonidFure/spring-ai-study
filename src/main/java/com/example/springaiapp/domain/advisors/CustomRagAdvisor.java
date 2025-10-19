package com.example.springaiapp.domain.advisors;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;

@Builder
@Deprecated(since = "нет необходимости писать свой advisor для rag, т.к. есть RetrievalAugmentationAdvisor")
public class CustomRagAdvisor implements BaseAdvisor {
    private static final PromptTemplate TEMPLATE = PromptTemplate.builder()
            .template("""
                        Context: {context},
                        Question: {question}
                    """)
            .build();

    private final VectorStore vectorStore;
    @Getter
    private final int order;

    public static CustomRagAdvisorBuilder builder(VectorStore vectorStore) {
        return new CustomRagAdvisorBuilder()
                .vectorStore(vectorStore);
    }

    @NonNull
    @Override
    public ChatClientRequest before(@NonNull ChatClientRequest chatClientRequest, @NonNull AdvisorChain advisorChain) {
        final var originalUserQuery = chatClientRequest.prompt().getUserMessage().getText();
        final var queryToRag = chatClientRequest.context().getOrDefault(
                ExpansionQueryAdvisor.ENRICHED_QUESTION,
                originalUserQuery).toString();
        final var searchRequest = SearchRequest.builder()
                .query(queryToRag)
                .similarityThreshold(0.65)
                .topK(4)
                .build();
        final var documents = vectorStore.similaritySearch(searchRequest);
        if (CollectionUtils.isEmpty(documents)) {
            return chatClientRequest;
        }
        final var llmContext = documents.stream()
                .map(it -> it.getText())
                .collect(Collectors.joining(System.lineSeparator()));

        final var prompt = TEMPLATE.render(Map.of("context", llmContext, "question", originalUserQuery));
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(prompt))
                .build();
    }

    @NonNull
    @Override
    public ChatClientResponse after(@NonNull ChatClientResponse chatClientResponse,
            @NonNull AdvisorChain advisorChain) {
        return chatClientResponse;
    }
}
