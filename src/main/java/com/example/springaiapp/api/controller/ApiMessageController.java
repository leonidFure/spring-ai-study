package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST API контроллер для работы с сообщениями
 * Обрабатывает AJAX запросы от фронтенда
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ApiMessageController {
    
    private final MessageService messageService;
    
    /**
     * Отправка сообщения
     * @param request данные сообщения
     * @return ответ AI
     */
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            MessageDto response = messageService.sendMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Получение сообщений чата
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageDto>> getMessagesByChatId(@PathVariable Long chatId) {
        try {
            List<MessageDto> messages = messageService.getMessagesByChatId(chatId, 0, 100).getContent();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
