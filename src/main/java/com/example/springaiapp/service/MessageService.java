package com.example.springaiapp.service;

import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.api.mapper.MessageMapper;
import com.example.springaiapp.infrastracture.entity.Message;
import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.infrastracture.repository.ChatRepository;
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
 * Сервис для работы с сообщениями
 * Содержит бизнес-логику для управления сообщениями
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatRepository chatRepository;
    
    
    /**
     * Получение сообщения по ID
     * @param id идентификатор сообщения
     * @return сообщение или пустой Optional
     */
    public Optional<MessageDto> getMessageById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDto);
    }
    
    /**
     * Получение всех сообщений в чате
     * @param chatId идентификатор чата
     * @return список сообщений в чате
     */
    public List<MessageDto> getMessagesByChatId(Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Получение сообщений в чате с пагинацией
     * @param chatId идентификатор чата
     * @param page номер страницы
     * @param size размер страницы
     * @return страница сообщений
     */
    public Page<MessageDto> getMessagesByChatId(Long chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable)
                .map(messageMapper::toDto);
    }
    
    
    /**
     * Поиск сообщений по содержимому
     * @param content содержимое для поиска
     * @return список найденных сообщений
     */
    public List<MessageDto> searchMessagesByContent(String content) {
        return messageRepository.findByContentContainingIgnoreCase(content).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    
    
    /**
     * Получение последнего сообщения в чате
     * @param chatId идентификатор чата
     * @return последнее сообщение или пустой Optional
     */
    public Optional<MessageDto> getLastMessageInChat(Long chatId) {
        List<Message> messages = messageRepository.findLastMessageInChat(chatId);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messageMapper.toDto(messages.get(0)));
    }
    
    /**
     * Получение сообщений, созданных после указанной даты в чате
     * @param chatId идентификатор чата
     * @param dateFrom дата начала периода
     * @return список сообщений, созданных после указанной даты
     */
    public List<MessageDto> getMessagesAfterDate(Long chatId, LocalDateTime dateFrom) {
        return messageRepository.findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(chatId, dateFrom).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Получение количества сообщений в чате
     * @param chatId идентификатор чата
     * @return количество сообщений в чате
     */
    public long getMessageCountInChat(Long chatId) {
        return messageRepository.countByChatId(chatId);
    }
    
    
    
    
    /**
     * Удаление всех сообщений в чате
     * @param chatId идентификатор чата
     */
    @Transactional
    public void deleteAllMessagesInChat(Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }
    
    /**
     * Отправка сообщения (пока без AI интеграции)
     * @param request данные сообщения
     * @return ответное сообщение
     */
    @Transactional
    public MessageDto sendMessage(SendMessageRequest request) {
        // Проверяем существование чата
        if (!chatRepository.existsById(request.getChatId())) {
            throw new IllegalArgumentException("Чат не найден");
        }
        
        // Создаем сообщение пользователя
        Message userMessage = new Message();
        userMessage.setChatId(request.getChatId());
        userMessage.setContent(request.getContent());
        userMessage.setRole(Message.MessageRole.USER);
        userMessage.setCreatedAt(LocalDateTime.now());
        
        messageRepository.save(userMessage);
        
        // Пока возвращаем простой ответ без AI
        Message aiMessage = new Message();
        aiMessage.setChatId(request.getChatId());
        aiMessage.setContent("Извините, AI интеграция пока не настроена. Ваше сообщение: " + request.getContent());
        aiMessage.setRole(Message.MessageRole.ASSISTANT);
        aiMessage.setCreatedAt(LocalDateTime.now());
        
        Message savedAiMessage = messageRepository.save(aiMessage);
        return messageMapper.toDto(savedAiMessage);
    }
}
