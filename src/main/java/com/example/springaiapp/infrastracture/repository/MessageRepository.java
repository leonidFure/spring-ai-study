package com.example.springaiapp.infrastracture.repository;

import com.example.springaiapp.infrastracture.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Message
 * Предоставляет методы для CRUD операций и поиска сообщений
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    /**
     * Поиск всех сообщений в указанном чате
     * @param chatId идентификатор чата
     * @return список сообщений в чате
     */
    List<MessageEntity> findByChatIdOrderByCreatedAtAsc(Long chatId);
    
    /**
     * Поиск сообщений в чате с пагинацией
     * @param chatId идентификатор чата
     * @param pageable параметры пагинации
     * @return страница сообщений
     */
    Page<MessageEntity> findByChatIdOrderByCreatedAtAsc(Long chatId, Pageable pageable);
    
    
    /**
     * Поиск сообщений по содержимому (без учета регистра)
     * @param content содержимое для поиска
     * @return список сообщений, содержащих указанный текст
     */
    List<MessageEntity> findByContentContainingIgnoreCase(String content);
    
    
    
    /**
     * Поиск сообщений, созданных после указанной даты в чате
     * @param chatId идентификатор чата
     * @param dateFrom дата начала периода
     * @return список сообщений, созданных после указанной даты
     */
    List<MessageEntity> findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(Long chatId, LocalDateTime dateFrom);
    
    /**
     * Подсчет количества сообщений в чате
     * @param chatId идентификатор чата
     * @return количество сообщений в чате
     */
    long countByChatId(Long chatId);
    
    
    /**
     * Поиск последнего сообщения в чате
     * @param chatId идентификатор чата
     * @return последнее сообщение в чате или null
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId ORDER BY m.createdAt DESC")
    List<MessageEntity> findLastMessageInChat(@Param("chatId") Long chatId);
    
        /**
        * Поиск последних n сообщений в чате
     * @param chatId идентификатор чата
     * @param n количество сообщений
     * @return последние n сообщений в чате или null
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId ORDER BY m.createdAt DESC LIMIT :n")
    List<MessageEntity> findLastMessageInChatTopN(@Param("chatId") Long chatId, @Param("n") int n);
    
    /**
     * Удаление всех сообщений в указанном чате
     * @param chatId идентификатор чата
     */
    void deleteByChatId(Long chatId);
}
