package com.example.springaiapp.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.springaiapp.service.ChatService;
import com.example.springaiapp.service.MessageService;

import lombok.RequiredArgsConstructor;

/**
 * Контроллер для работы с сообщениями через Thymeleaf
 * Обрабатывает HTTP запросы и возвращает HTML страницы
 */
@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ChatService chatService;

    /**
     * Страница сообщений в конкретном чате
     * 
     * @param chatId идентификатор чата
     * @param page   номер страницы (по умолчанию 0)
     * @param size   размер страницы (по умолчанию 20)
     * @param model  модель для передачи данных в представление
     * @return имя шаблона или редирект
     */
    @GetMapping("/chat/{chatId}")
    public String getMessagesInChat(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        // Проверяем существование чата
        return chatService.getChatById(chatId)
                .map(chat -> {
                    final var messages = messageService.getMessagesByChatId(chatId, page, size);
                    model.addAttribute("chat", chat);
                    model.addAttribute("messages", messages.getContent());
                    model.addAttribute("currentPage", page);
                    model.addAttribute("totalPages", messages.getTotalPages());
                    model.addAttribute("totalElements", messages.getTotalElements());
                    model.addAttribute("messageCount", messageService.getMessageCountInChat(chatId));
                    return "messages/chat";
                })
                .orElse("redirect:/chats?error=chat_not_found");
    }

}
