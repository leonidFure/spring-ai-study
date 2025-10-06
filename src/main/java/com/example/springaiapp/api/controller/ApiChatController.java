package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
import com.example.springaiapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

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
     * @return список чатов
     */
    @GetMapping
    public ResponseEntity<List<ChatDto>> getAllChats() {
        List<ChatDto> chats = chatService.getAllChats(0, 100).getContent();
        return ResponseEntity.ok(chats);
    }
    
    /**
     * Создание нового чата
     * @param request данные для создания чата
     * @return созданный чат
     */
    @PostMapping
    public ResponseEntity<ChatDto> createChat(@Valid @RequestBody CreateChatRequest request) {
        try {
            ChatDto createdChat = chatService.createChat(request);
            return ResponseEntity.ok(createdChat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Получение чата по ID
     * @param id идентификатор чата
     * @return чат или 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long id) {
        Optional<ChatDto> chat = chatService.getChatById(id);
        return chat.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Удаление чата
     * @param id идентификатор чата
     * @return статус операции
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        try {
            boolean deleted = chatService.deleteChat(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
