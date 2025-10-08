package com.example.springaiapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
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
                .defaultAdvisors(chatMemoryAdvisor(), ragAdvisor())
                .build();
    }

    private Advisor chatMemoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(postgresChatMemory())
                .build();
    }

    private Advisor ragAdvisor() {
        return QuestionAnswerAdvisor.builder(vectorStore)
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
