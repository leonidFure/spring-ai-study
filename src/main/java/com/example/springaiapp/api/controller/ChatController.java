package com.example.springaiapp.api.controller;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
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
 * Контроллер для работы с чатами через Thymeleaf
 * Обрабатывает HTTP запросы и возвращает HTML страницы
 */
@Controller
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * Главная страница со списком всех чатов
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 10)
     * @param search поисковый запрос (опционально)
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping
    public String getAllChats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<ChatDto> chats  = chatService.getAllChats(page, size);
        model.addAttribute("chats", chats.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", chats.getTotalPages());
        model.addAttribute("totalElements", chats.getTotalElements());
        model.addAttribute("isSearch", false);
        
        model.addAttribute("chatCount", chatService.getChatCount());
        return "chats/list";
    }
    
    /**
     * Страница создания нового чата
     * @param model модель для передачи данных в представление
     * @return имя шаблона
     */
    @GetMapping("/new")
    public String showCreateChatForm(Model model) {
        model.addAttribute("createChatRequest", new CreateChatRequest());
        return "chats/create";
    }
    
    /**
     * Обработка создания нового чата
     * @param request данные для создания чата
     * @param bindingResult результаты валидации
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на страницу чата или форму создания
     */
    @PostMapping("/new")
    public String createChat(
            @Valid @ModelAttribute CreateChatRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "chats/create";
        }
        
        try {
            ChatDto createdChat = chatService.createChat(request);
            redirectAttributes.addFlashAttribute("successMessage", "Чат успешно создан!");
            return "redirect:/chats/" + createdChat.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании чата: " + e.getMessage());
            return "redirect:/chats/new";
        }
    }
    
    /**
     * Страница просмотра конкретного чата с сообщениями
     * @param id идентификатор чата
     * @param model модель для передачи данных в представление
     * @return имя шаблона или редирект
     */
    @GetMapping("/{id}")
    public String getChat(@PathVariable Long id, Model model) {
        Optional<ChatDto> chatOpt = chatService.getChatWithMessages(id);
        
        if (chatOpt.isEmpty()) {
            return "redirect:/chats?error=chat_not_found";
        }
        
        ChatDto chat = chatOpt.get();
        model.addAttribute("chat", chat);
        model.addAttribute("messageCount", chat.getMessageCount());
        
        return "chats/detail";
    }
    
    
    /**
     * Удаление чата
     * @param id идентификатор чата
     * @param redirectAttributes атрибуты для редиректа
     * @return редирект на список чатов
     */
    @PostMapping("/{id}/delete")
    public String deleteChat(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = chatService.deleteChat(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage", "Чат успешно удален!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Чат не найден");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении чата: " + e.getMessage());
        }
        
        return "redirect:/chats";
    }
    
}
