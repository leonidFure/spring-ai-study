package com.example.springaiapp.domain.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * Model для загруженного контента
 * Immutable модель данных
 */
@Data
@Builder
public class LoadedContentModel {
    private final Long id;
    private final String filename;
    private final String hash;
    private final String type;
    private final Integer chunkCount;
    private final LocalDateTime loadedAt;
}
