package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
import com.example.springaiapp.domain.service.ChatService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST API контроллер для работы с чатами
 * Обрабатывает AJAX запросы от фронтенда
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ApiChatController {

    private final ChatService chatService;

    /**
     * Получение всех чатов
     * 
     * @return список чатов
     */
    @GetMapping
    public ResponseEntity<List<ChatDto>> getAllChats() {
        return ResponseEntity.ok(chatService.getAllChats(0, 100).getContent());
    }

    /**
     * Создание нового чата
     * 
     * @param request данные для создания чата
     * @return созданный чат
     */
    @PostMapping
    public ResponseEntity<ChatDto> createChat(@Valid @RequestBody CreateChatRequest request) {
        try {
            return ResponseEntity.ok(chatService.createChat(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получение чата по ID
     * 
     * @param id идентификатор чата
     * @return чат или 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long id) {
        return chatService.getChatById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Удаление чата
     * 
     * @param id идентификатор чата
     * @return статус операции
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        try {
            return chatService.deleteChat(id)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
