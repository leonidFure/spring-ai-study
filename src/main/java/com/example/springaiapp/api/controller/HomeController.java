package com.example.springaiapp.api.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.springaiapp.service.ChatService;
import com.example.springaiapp.service.MessageService;

import lombok.RequiredArgsConstructor;

/**
 * Главный контроллер для отображения чат-интерфейса
 * Обрабатывает главную страницу приложения
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ChatService chatService;
    private final MessageService messageService;

    /**
     * Главная страница с чат-интерфейсом
     * 
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping("/")
    public String home(Model model) {
        // Получаем список всех чатов для боковой панели (сортируем по дате создания, новые первыми)
        final var chats = chatService.getAllChats(0, 10).getContent();
        model.addAttribute("chats", chats);
        model.addAttribute("chatCount", chatService.getChatCount());

        // Получаем последний чат (самый новый) и его сообщения
        final var lastChat = !chats.isEmpty() ? chats.get(0) : null;
        final var messages = lastChat != null
                ? messageService.getMessagesByChatId(lastChat.getId())
                : List.of();
        
        model.addAttribute("messages", messages);
        model.addAttribute("lastChatId", lastChat != null ? lastChat.getId() : null);
        
        // Отладочная информация
        System.out.println("Количество чатов: " + chats.size());
        if (lastChat != null) {
            System.out.println("Последний чат ID: " + lastChat.getId() + ", название: " + lastChat.getTitle());
        } else {
            System.out.println("Чатов нет");
        }

        return "index";
    }
}
