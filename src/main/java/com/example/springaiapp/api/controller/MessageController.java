package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.Message;
import com.example.springaiapp.service.ChatService;
import com.example.springaiapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

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
     * @param chatId идентификатор чата
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 20)
     * @param model модель для передачи данных в представление
     * @return имя шаблона или редирект
     */
    @GetMapping("/chat/{chatId}")
    public String getMessagesInChat(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        // Проверяем существование чата
        Optional<ChatDto> chatOpt = chatService.getChatById(chatId);
        if (chatOpt.isEmpty()) {
            return "redirect:/chats?error=chat_not_found";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageDto> messages = messageService.getMessagesByChatId(chatId, page, size);
        
        model.addAttribute("chat", chatOpt.get());
        model.addAttribute("messages", messages.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messages.getTotalPages());
        model.addAttribute("totalElements", messages.getTotalElements());
        model.addAttribute("messageCount", messageService.getMessageCountInChat(chatId));
        
        return "messages/chat";
    }
    
    
    
    
    
    
    
}
