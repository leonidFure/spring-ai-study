package com.example.springaiapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springaiapp.domain.joiners.BM25DocumentPostProcessor;
import com.example.springaiapp.domain.service.impl.MessageMapperService;
import com.example.springaiapp.domain.service.impl.PostgresChatMemoryService;
import com.example.springaiapp.domain.transformers.ExpansionQueryTransformer;
import com.example.springaiapp.infrastracture.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

        private final OllamaChatModel ollamaChatModel;
        private final VectorStore vectorStore;
        private final MessageRepository messageRepository;
        private final MessageMapperService messageMapperService;

        @Value("${app.chat.rag.top-k}")
        private int topK;
        @Value("${app.chat.rag.similarity-threshold}")
        private double similarityThreshold;
        @Value("${app.chat.memory.max-messages}")
        private long maxMessages;

        @Bean
        public ChatClient chatClient() {
                return ChatClient.builder(ollamaChatModel)
                                // advisor для памяти модели должен работать перед advisor для векторного
                                // хранилища, чтобы в памяти модели был контекст перед тем,
                                // как идти в векторное хранилище
                                .defaultSystem("Ты - Горев Леонид, Java разработчик, ты должен отвечать кратко и лаконично, не используя эмоции и не используя слишком много слов.")
                                .defaultAdvisors(
                                                messageChatMemoryAdvisor(10),
                                                simpleLoggerAdvisor(20),
                                                retrievalAugmentationAdvisor(30),
                                                simpleLoggerAdvisor(40))
                                .defaultOptions(ChatOptions.builder()
                                                // ограничевает выбор K самых вероятных токенов, из которых потом
                                                // выбирается
                                                // следующий.
                                                // k=1 - всегда выбирается 1 самый вероятный токен
                                                // k=10 - выбирается 10 самых вероятных токенов
                                                // Рекомендации:
                                                // 1-10 - для стабильных, точных ответов
                                                // 40-100 - для более вариативных/творческих ответов
                                                // .topK(30)
                                                // выбирает набор токенов, чья суммарная вероятность не превышает topP
                                                // topP=1.0 - все токены учитываются
                                                // topP=0.7 - учитываются только 70% самых вероятных токенов
                                                // Рекомендации:
                                                // 0.8–0.95 - баланса реалистичности и креативности
                                                // 0.5–0.8 - для строгих, точных ответов
                                                .topP(0.9)
                                                // регулирует случайность (креативность) ответов
                                                // 0.0 - самый детерминированный ответ
                                                // 1.0 - самый случайный ответ
                                                // Рекомендации:
                                                // 0-0.3 - технические задачи
                                                // 0.7-1.0 - творческие задачи
                                                // 0.3-0.7 - средний уровень
                                                .temperature(0.8)
                                                .build())
                                .build();
        }

        private Advisor simpleLoggerAdvisor(int order) {
                return SimpleLoggerAdvisor.builder()
                                .order(order)
                                .build();
        }

        // представление памяти в рамках чата в реляционной бд
        // MessageChatMemoryAdvisor позволяет реализовать это по разному,
        // но тут именно в пг
        private Advisor messageChatMemoryAdvisor(int order) {
                return MessageChatMemoryAdvisor.builder(
                                PostgresChatMemoryService.builder()
                                                .maxMessages(maxMessages)
                                                .messageRepository(messageRepository)
                                                .messageMapperService(messageMapperService)
                                                .build())
                                .order(order)
                                .build();
        }

        // RetrievalAugmentationAdvisor - более продвинутая версия чем
        // QuestionAnswerAdvisor
        // но тут больше настроек
        // например можно настраивать реакцию на то, что информация не найдена в
        // контексте
        private Advisor retrievalAugmentationAdvisor(int order) {
                return RetrievalAugmentationAdvisor.builder()
                                // настройка взаимодействия с векторным хранилищем
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                                // // 0..1, чем больше, тем строже (меньше документов будет
                                                // возвращаться)
                                                .topK(topK * 2)
                                                .similarityThreshold(similarityThreshold)
                                                .vectorStore(vectorStore)
                                                .build())
                                // как понял здесь модификация промптов идет по необходимости
                                // пока не понятно, делается это только тут или можно по стандарту настроить на
                                // входящие промпты
                                // .queryTransformers(null)
                                .queryTransformers(ExpansionQueryTransformer.builder(ollamaChatModel)
                                                .build())
                                .queryAugmenter(ContextualQueryAugmenter.builder()
                                                // true - если в контексте ничего не найдено, запрос полностью переходит
                                                // на плечи ии (если есть ответ в его базе, то он его даст)
                                                // false - если в контексте ничего не найден, выполняется
                                                // emptyContextPromptTemplate
                                                .allowEmptyContext(true)
                                                // .emptyContextPromptTemplate(new PromptTemplate(
                                                // "Запрос вне твоей базы знаний, расскажи стишок про это пользователю
                                                // из 5 строк"))
                                                .build())
                                .documentPostProcessors(BM25DocumentPostProcessor.builder()
                                                .limit(topK)
                                                .build())
                                .order(order)
                                .build();
        }

        // // представление памяти в рамках чата в векторном хранилище
        // private Advisor
        // vectorStoreChatMemoryAdvisorBuilder(VectorStore vectorStore) {
        // return VectorStoreChatMemoryAdvisor.builder(vectorStore);
        // }
}
