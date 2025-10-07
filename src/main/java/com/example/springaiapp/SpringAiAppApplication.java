package com.example.springaiapp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Главный класс Spring Boot приложения с поддержкой Spring AI и Ollama
 */
@SpringBootApplication
public class SpringAiAppApplication {

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringAiAppApplication.class, args);
    }

}
