package com.example.springaiapp.service;

import com.example.springaiapp.api.dto.ResponeMessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с сообщениями
 * Содержит бизнес-логику для управления сообщениями
 */
public interface MessageService {
    
    /**
     * Получение сообщения по ID
     * 
     * @param id идентификатор сообщения
     * @return сообщение или пустой Optional
     */
    Optional<ResponeMessageDto> getMessageById(Long id);

    /**
     * Получение всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     * @return список сообщений в чате
     */
    List<ResponeMessageDto> getMessagesByChatId(Long chatId);

    /**
     * Получение сообщений в чате с пагинацией
     * 
     * @param chatId идентификатор чата
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница сообщений
     */
    Page<ResponeMessageDto> getMessagesByChatId(Long chatId, int page, int size);

    /**
     * Поиск сообщений по содержимому
     * 
     * @param content содержимое для поиска
     * @return список найденных сообщений
     */
    List<ResponeMessageDto> searchMessagesByContent(String content);

    /**
     * Получение последнего сообщения в чате
     * 
     * @param chatId идентификатор чата
     * @return последнее сообщение или пустой Optional
     */
    Optional<ResponeMessageDto> getLastMessageInChat(Long chatId);

    /**
     * Получение сообщений, созданных после указанной даты в чате
     * 
     * @param chatId   идентификатор чата
     * @param dateFrom дата начала периода
     * @return список сообщений, созданных после указанной даты
     */
    List<ResponeMessageDto> getMessagesAfterDate(Long chatId, LocalDateTime dateFrom);

    /**
     * Получение количества сообщений в чате
     * 
     * @param chatId идентификатор чата
     * @return количество сообщений в чате
     */
    long getMessageCountInChat(Long chatId);

    /**
     * Удаление всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     */
    void deleteAllMessagesInChat(Long chatId);

    /**
     * Отправка сообщения (пока без AI интеграции)
     * 
     * @param request данные сообщения
     * @return ответное сообщение
     */
    ResponeMessageDto sendMessage(SendMessageRequest request);

    /**
     * Создание сообщения пользователя
     * 
     * @param request данные сообщения
     * @return созданное сообщение пользователя
     */
    MessageEntity createUserMessage(SendMessageRequest request);

    /**
     * Сохранение сообщения
     * 
     * @param message сообщение для сохранения
     * @return сохраненное сообщение
     */
    MessageEntity saveMessage(MessageEntity message);

    /**
     * Создание сообщения ассистента
     * 
     * @param chatId  идентификатор чата
     * @param content содержимое сообщения
     * @return созданное сообщение ассистента
     */
    MessageEntity createAssistantMessage(Long chatId, String content);

    /**
     * Генерация потокового ответа (имитация)
     * 
     * @param request данные сообщения
     * @return сгенерированный ответ
     */
    SseEmitter generateStreamingResponse(SendMessageRequest request);
}