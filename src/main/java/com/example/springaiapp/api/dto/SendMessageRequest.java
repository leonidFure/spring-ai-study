package com.example.springaiapp.api.dto;

import org.springframework.ai.chat.messages.MessageType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса отправки сообщения
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    @NotNull(message = "ID чата обязателен")
    private Long chatId;
    
    @NotNull(message = "Роль сообщения обязательна")
    private MessageType role;
    
    @NotBlank(message = "Содержимое сообщения не может быть пустым")
    private String content;
}
