package com.example.springaiapp.config;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.chat.rag", value = "enabled", havingValue = "true")
public class RagConfig {

        private final VectorStore vectorStore;

        @Value("${app.chat.rag.top-k}")
        private int topK;
        @Value("${app.chat.rag.similarity-threshold}")
        private double similarityThreshold;

        @Bean
        public Advisor questionAnswerAdvisor() {
                return QuestionAnswerAdvisor.builder(vectorStore)
                                .order(30)
                                // для настройки промпта,с которым ллм идет в векторное хранилище
                                // .promptTemplate(new PromptTemplate("TODOD"))
                                .searchRequest(SearchRequest.builder()
                                                // сколько взять документов из rag
                                                .topK(topK)
                                                // // 0..1, чем больше, тем строже (меньше документов будет
                                                // возвращаться)
                                                .similarityThreshold(similarityThreshold)
                                                // // мета-фильтр
                                                // .filterExpression("source == 'docs/webflux_guide.txt'")
                                                .build())
                                .build();
        }

        // RetrievalAugmentationAdvisor - более продвинутая версия чем
        // QuestionAnswerAdvisor
        // но тут больше настроек
        // например можно настраивать реакцию на то, что информация не найдена в
        // контексте
        @Bean
        public Advisor retrievalAugmentationAdvisor() {
                return RetrievalAugmentationAdvisor.builder()
                                .order(30)
                                // настройка взаимодействия с векторным хранилищем
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                                // // 0..1, чем больше, тем строже (меньше документов будет
                                                // возвращаться)
                                                .topK(topK)
                                                .similarityThreshold(similarityThreshold)
                                                .vectorStore(vectorStore)
                                                .build())
                                // как понял здесь модификация промптов идет по необходимости
                                // пока не понятно, делается это только тут или можно по стандарту настроить на
                                // входящие промпты
                                // .queryTransformers(null)
                                .queryAugmenter(ContextualQueryAugmenter.builder()
                                                // true - если в контексте ничего не найдено, запрос полностью переходит
                                                // на плечи ии (если есть ответ в его базе, то он его даст)
                                                // false - если в контексте ничего не найден, выполняется
                                                // emptyContextPromptTemplate
                                                .allowEmptyContext(false)
                                                .emptyContextPromptTemplate(new PromptTemplate(
                                                                "Запрос вне твоей базы знаний, расскажи стишок про это пользователю из 5 строк"))
                                                .build())
                                .build();
        }
}
