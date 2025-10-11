package com.example.springaiapp.domain.service;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс сервиса для работы с чатами
 * Содержит бизнес-логику для управления чатами
 */
public interface ChatService {
    
    /**
     * Создание нового чата
     * @param request данные для создания чата
     * @return созданный чат
     */
    ChatDto createChat(CreateChatRequest request);
    
    /**
     * Получение чата по ID
     * @param id идентификатор чата
     * @return чат или пустой Optional
     */
    Optional<ChatDto> getChatById(Long id);
    
    /**
     * Получение чата с сообщениями по ID
     * @param id идентификатор чата
     * @return чат с сообщениями или пустой Optional
     */
    Optional<ChatDto> getChatWithMessages(Long id);
    
    /**
     * Получение всех чатов с пагинацией (отсортированных по дате создания, новые первыми)
     * @param page номер страницы
     * @param size размер страницы
     * @return страница чатов
     */
    Page<ChatDto> getAllChats(int page, int size);
    
    /**
     * Получение всех чатов
     * @return список всех чатов
     */
    List<ChatDto> getAllChats();
    
    /**
     * Поиск чатов по заголовку
     * @param title заголовок для поиска
     * @return список найденных чатов
     */
    List<ChatDto> searchChatsByTitle(String title);
    
    /**
     * Удаление чата
     * @param id идентификатор чата
     * @return true если чат был удален, false если не найден
     */
    boolean deleteChat(Long id);
    
    /**
     * Получение количества чатов
     * @return общее количество чатов
     */
    long getChatCount();
}