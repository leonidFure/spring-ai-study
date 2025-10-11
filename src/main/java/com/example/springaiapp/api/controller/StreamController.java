package com.example.springaiapp.api.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.domain.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Контроллер для стриминговых сообщений через SSE
 * Обеспечивает потоковую передачу ответов AI в реальном времени
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
@CrossOrigin(origins = "http://localhost:8080")
@RequiredArgsConstructor
public class StreamController {
    private final MessageService messageService;


    /**
     * Отправка сообщения с потоковым ответом через SSE
     * 
     * @param request данные сообщения
     * @return SSE поток для стриминга ответа
     */
    @PostMapping(value = "/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(@RequestBody SendMessageRequest request) {
        log.info("Получен запрос на стриминг: {}", request);
        return messageService.generateStreamingResponse(request);
    }

    /**
     * Проверка статуса SSE соединения
     * 
     * @return статус соединения
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("SSE сервис активен");
    }
}
