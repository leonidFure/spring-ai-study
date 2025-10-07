package com.example.springaiapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.mappers.MessageMapperService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfiguration {

    private final MessageRepository messageRepository;
    private final MessageMapperService messageMapperService;
    @Qualifier("postgresChatMemory")
    private final ChatMemory postgresChatMemory;

    @Value("${app.max-messages:2}")
    private long maxMessages;
    
    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(postgresChatMemory())
                        .build())
                .build();
    }

    private PostgresChatMemory postgresChatMemory() {
        return PostgresChatMemory.builder()
                .maxMessages(maxMessages)
                .messageRepository(messageRepository)
                .messageMapperService(messageMapperService)
                .build();
    }
}
