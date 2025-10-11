package com.example.springaiapp.config;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.springaiapp.domain.service.impl.MessageMapperService;
import com.example.springaiapp.domain.service.impl.PostgresChatMemoryService;
import com.example.springaiapp.infrastracture.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

/**
 * Конфигурация памяти чата
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.chat.memory", value = "enabled", havingValue = "true")
public class ChatMemoryConfig {

        // представление памяти в рамках чата в векторном хранилище
        @Bean
        @ConditionalOnProperty(prefix = "app.chat.memory", value = "type", havingValue = "vector", matchIfMissing = true)
        public BaseChatMemoryAdvisor vectorChatMemoryAdvisor(VectorStore vectorStore) {
                return VectorStoreChatMemoryAdvisor.builder(vectorStore)
                                .order(10)
                                .build();
        }

        // представление памяти в рамках чата в реляционной бд
        // MessageChatMemoryAdvisor позволяет реализовать это по разному,
        // но тут именно в пг
        @Bean
        @ConditionalOnProperty(prefix = "app.chat.memory", value = "type", havingValue = "postgres")
        public BaseChatMemoryAdvisor postgresChatMemoryAdvisor(
                        MessageRepository messageRepository,
                        MessageMapperService messageMapperService,
                        @Value("${app.chat.memory.max-messages}") long maxMessages) {
                return MessageChatMemoryAdvisor.builder(
                                PostgresChatMemoryService.builder()
                                                .maxMessages(maxMessages)
                                                .messageRepository(messageRepository)
                                                .messageMapperService(messageMapperService)
                                                .build())
                                .order(10)
                                .build();
        }
}
