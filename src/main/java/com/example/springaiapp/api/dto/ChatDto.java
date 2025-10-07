package com.example.springaiapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для передачи данных о чате
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private List<ResponeMessageDto> messages;
    private Long messageCount;
    
    /**
     * Конструктор для создания DTO без сообщений
     */
    public ChatDto(Long id, String title, LocalDateTime createdAt, Long messageCount) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
    }
}
