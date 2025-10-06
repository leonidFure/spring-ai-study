package com.example.springaiapp.api.dto;

import com.example.springaiapp.infrastracture.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    private Message.MessageRole role;
    
    @NotBlank(message = "Содержимое сообщения не может быть пустым")
    private String content;
}
