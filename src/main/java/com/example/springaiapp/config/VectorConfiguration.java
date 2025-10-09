package com.example.springaiapp.config;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorConfiguration {

    /**
     * Разбивает текст на чанки
     * @return TextSplitter
     */
    @Bean
    public TextSplitter textSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(250) // todo сделать configurable
                .build();
    }
}
