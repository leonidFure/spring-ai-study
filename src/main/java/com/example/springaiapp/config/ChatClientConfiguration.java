package com.example.springaiapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springaiapp.domain.loadedcontent.service.impl.MessageMapperService;
import com.example.springaiapp.domain.loadedcontent.service.impl.PostgresChatMemoryService;
import com.example.springaiapp.infrastracture.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfiguration {

    private final MessageRepository messageRepository;
    private final MessageMapperService messageMapperService;
    @Qualifier("postgresChatMemory")
    private final ChatMemory postgresChatMemory;
    private final VectorStore vectorStore;

    @Value("${app.max-messages:2}")
    private long maxMessages; // количество сообщений в памяти модели

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                // advisor для памяти модели должен работать перед advisor для векторного
                // хранилища, чтобы в памяти модели был контекст перед тем,
                // как идти в векторное хранилище
                .defaultAdvisors(chatMemoryAdvisor(),
                        SimpleLoggerAdvisor.builder().build(), 
                        ragAdvisor(),
                        SimpleLoggerAdvisor.builder().build())
                .defaultOptions(ChatOptions.builder()
                        // ограничевает выбор K самых вероятных токенов, из которых потом выбирается
                        // следующий.
                        // k=1 - всегда выбирается 1 самый вероятный токен
                        // k=10 - выбирается 10 самых вероятных токенов
                        // Рекомендации:
                        // 1-10 - для стабильных, точных ответов
                        // 40-100 - для более вариативных/творческих ответов
                        .topK(20)
                        // выбирает набор токенов, чья суммарная вероятность не превышает topP
                        // topP=1.0 - все токены учитываются
                        // topP=0.7 - учитываются только 70% самых вероятных токенов
                        // Рекомендации:
                        // 0.8–0.95 - баланса реалистичности и креативности
                        // 0.5–0.8 - для строгих, точных ответов
                        .topP(0.7)
                        // регулирует случайность (креативность) ответов
                        // 0.0 - самый детерминированный ответ
                        // 1.0 - самый случайный ответ
                        // Рекомендации:
                        // 0-0.3 - технические задачи
                        // 0.7-1.0 - творческие задачи
                        // 0.3-0.7 - средний уровень
                        .temperature(0.3)
                        .build())
                .build();
    }

    private Advisor chatMemoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(postgresChatMemory())
                .build();
    }

    private Advisor ragAdvisor() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                // для настройки промпта,с которым ллм идет в векторное хранилище
                // .promptTemplate(new PromptTemplate("TODOD"))
                // .searchRequest(SearchRequest.builder()
                // // количество документов, которые будут возвращаться
                // .topK(5)
                // // 0..1, чем больше, тем строже (меньше документов будет возвращаться)
                // .similarityThreshold(0.75)
                // // мета-фильтр
                // .filterExpression("source == 'docs/webflux_guide.txt'")
                // .build())
                .build();
    }

    private PostgresChatMemoryService postgresChatMemory() {
        return PostgresChatMemoryService.builder()
                .maxMessages(maxMessages)
                .messageRepository(messageRepository)
                .messageMapperService(messageMapperService)
                .build();
    }
}
