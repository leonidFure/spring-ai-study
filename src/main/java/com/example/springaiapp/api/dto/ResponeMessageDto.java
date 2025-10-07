package com.example.springaiapp.api.dto;

import com.example.springaiapp.infrastracture.entity.MessageEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.ai.chat.messages.MessageType;

/**
 * DTO для передачи данных о сообщении
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponeMessageDto {
    private Long id;
    private Long chatId;
    private MessageType role;
    private String content;
    private LocalDateTime createdAt;
    
    /**
     * Конструктор для создания DTO из Entity
     */
    public ResponeMessageDto(MessageEntity message) {
        this.id = message.getId();
        this.chatId = message.getChatId();
        this.role = message.getRole();
        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
    }
}
