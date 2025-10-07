package com.example.springaiapp.service;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
import com.example.springaiapp.api.mapper.ChatMapper;
import com.example.springaiapp.infrastracture.entity.Chat;
import com.example.springaiapp.infrastracture.repository.ChatRepository;
import com.example.springaiapp.infrastracture.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для работы с чатами
 * Содержит бизнес-логику для управления чатами
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatMapper chatMapper;
    
    /**
     * Создание нового чата
     * @param request данные для создания чата
     * @return созданный чат
     */
    @Transactional
    public ChatDto createChat(CreateChatRequest request) {
        final var chat = chatMapper.toEntity(request);
        chat.setCreatedAt(LocalDateTime.now());
        
        final var savedChat = chatRepository.save(chat);
        return chatMapper.toDto(savedChat);
    }
    
    /**
     * Получение чата по ID
     * @param id идентификатор чата
     * @return чат или пустой Optional
     */
    public Optional<ChatDto> getChatById(Long id) {
        return chatRepository.findById(id)
                .map(chatMapper::toDto);
    }
    
    /**
     * Получение чата с сообщениями по ID
     * @param id идентификатор чата
     * @return чат с сообщениями или пустой Optional
     */
    public Optional<ChatDto> getChatWithMessages(Long id) {
        return chatRepository.findByIdWithMessages(id)
                .map(chatMapper::toDtoWithMessages);
    }
    
    /**
     * Получение всех чатов с пагинацией (отсортированных по дате создания, новые первыми)
     * @param page номер страницы
     * @param size размер страницы
     * @return страница чатов
     */
    public Page<ChatDto> getAllChats(int page, int size) {
        final var pageable = PageRequest.of(page, size);
        return chatRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(chatMapper::toDto);
    }
    
    /**
     * Получение всех чатов
     * @return список всех чатов
     */
    public List<ChatDto> getAllChats() {
        return chatRepository.findAll().stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Поиск чатов по заголовку
     * @param title заголовок для поиска
     * @return список найденных чатов
     */
    public List<ChatDto> searchChatsByTitle(String title) {
        return chatRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Удаление чата
     * @param id идентификатор чата
     * @return true если чат был удален, false если не найден
     */
    @Transactional
    public boolean deleteChat(Long id) {
        if (chatRepository.existsById(id)) {
            // Сначала удаляем все сообщения чата
            messageRepository.deleteByChatId(id);
            // Затем удаляем сам чат
            chatRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Получение количества чатов
     * @return общее количество чатов
     */
    public long getChatCount() {
        return chatRepository.countAllChats();
    }
    
}
