package com.example.springaiapp.config;

import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TextSplitterConfig {

    @Value("${app.chunk-size}")
    private int chunkSize;
    /**
     * Разбивает текст на чанки
     * @return TextSplitter
     */
    @Bean
    public TextSplitter textSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(chunkSize) // todo сделать configurable
                .build();
    }
}
