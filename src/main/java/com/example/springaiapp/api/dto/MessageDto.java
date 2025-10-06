package com.example.springaiapp.api.dto;

import com.example.springaiapp.infrastracture.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о сообщении
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    
    private Long id;
    private Long chatId;
    private Message.MessageRole role;
    private String content;
    private LocalDateTime createdAt;
    
    /**
     * Конструктор для создания DTO из Entity
     */
    public MessageDto(Message message) {
        this.id = message.getId();
        this.chatId = message.getChatId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }
}
