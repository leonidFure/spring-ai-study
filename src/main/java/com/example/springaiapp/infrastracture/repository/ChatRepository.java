package com.example.springaiapp.infrastracture.repository;

import com.example.springaiapp.infrastracture.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Chat
 * Предоставляет методы для CRUD операций и поиска чатов
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    
    /**
     * Поиск чатов по заголовку (без учета регистра)
     * @param title заголовок чата
     * @return список чатов с указанным заголовком
     */
    List<Chat> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Поиск чатов, созданных после указанной даты
     * @param dateFrom дата начала периода
     * @return список чатов, созданных после указанной даты
     */
    List<Chat> findByCreatedAtAfter(LocalDateTime dateFrom);
    
    /**
     * Поиск чатов, созданных в указанном диапазоне дат
     * @param dateFrom дата начала периода
     * @param dateTo дата окончания периода
     * @return список чатов в указанном диапазоне
     */
    List<Chat> findByCreatedAtBetween(LocalDateTime dateFrom, LocalDateTime dateTo);
    
    /**
     * Поиск чата с сообщениями (с eager загрузкой)
     * @param id идентификатор чата
     * @return Optional с чатом и его сообщениями
     */
    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.messages WHERE c.id = :id")
    Optional<Chat> findByIdWithMessages(@Param("id") Long id);
    
    /**
     * Подсчет количества чатов
     * @return общее количество чатов
     */
    @Query("SELECT COUNT(c) FROM Chat c")
    long countAllChats();
    
}
