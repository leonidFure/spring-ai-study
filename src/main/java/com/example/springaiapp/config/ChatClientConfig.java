package com.example.springaiapp.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseChatMemoryAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

        @Bean
        public ChatClient chatClient(OllamaChatModel ollamaChatModel,
                        BaseChatMemoryAdvisor chatMemoryAdvisor,
                        Advisor retrievalAugmentationAdvisor) {
                return ChatClient.builder(ollamaChatModel)
                                // advisor для памяти модели должен работать перед advisor для векторного
                                // хранилища, чтобы в памяти модели был контекст перед тем,
                                // как идти в векторное хранилище
                                .defaultAdvisors(chatMemoryAdvisor,
                                                SimpleLoggerAdvisor.builder().order(20).build(),
                                                retrievalAugmentationAdvisor,
                                                SimpleLoggerAdvisor.builder().order(40).build())
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
}
