package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.service.ChatService;
import com.example.springaiapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping("/")
    public String home(Model model) {
        // Получаем список всех чатов для боковой панели
        List<ChatDto> chats = chatService.getAllChats(0, 10).getContent();
        model.addAttribute("chats", chats);
        model.addAttribute("chatCount", chatService.getChatCount());
        
        // Получаем сообщения первого чата (если есть)
        List<MessageDto> messages = List.of();
        if (!chats.isEmpty()) {
            messages = messageService.getMessagesByChatId(chats.get(0).getId());
        }
        model.addAttribute("messages", messages);
        
        return "index";
    }
}
