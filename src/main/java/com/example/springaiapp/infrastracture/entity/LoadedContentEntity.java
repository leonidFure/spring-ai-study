package com.example.springaiapp.infrastracture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity для таблицы loaded_content
 * Содержит информацию о загруженном контенте
 */
@Entity
@Table(name = "loaded_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadedContentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filename", nullable = false, length = 255)
    private String filename;
    
    @Column(name = "hash", nullable = false, length = 64)
    private String hash;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "chunk_count", nullable = false)
    private Integer chunkCount;
    
    @Column(name = "loaded_at", nullable = false, updatable = false)
    private LocalDateTime loadedAt;
}
