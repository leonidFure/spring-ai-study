package com.example.springaiapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса создания нового чата
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRequest {
    @NotBlank(message = "Заголовок чата не может быть пустым")
    @Size(max = 255, message = "Заголовок чата не может превышать 255 символов")
    private String title;
}
