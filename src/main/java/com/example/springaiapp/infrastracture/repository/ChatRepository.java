package com.example.springaiapp.infrastracture.repository;

import com.example.springaiapp.infrastracture.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    
    /**
     * Поиск чатов по заголовку (без учета регистра)
     * @param title заголовок чата
     * @return список чатов с указанным заголовком
     */
    List<ChatEntity> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Поиск чатов, созданных после указанной даты
     * @param dateFrom дата начала периода
     * @return список чатов, созданных после указанной даты
     */
    List<ChatEntity> findByCreatedAtAfter(LocalDateTime dateFrom);
    
    /**
     * Поиск чатов, созданных в указанном диапазоне дат
     * @param dateFrom дата начала периода
     * @param dateTo дата окончания периода
     * @return список чатов в указанном диапазоне
     */
    List<ChatEntity> findByCreatedAtBetween(LocalDateTime dateFrom, LocalDateTime dateTo);
    
    /**
     * Поиск чата с сообщениями (с eager загрузкой)
     * @param id идентификатор чата
     * @return Optional с чатом и его сообщениями
     */
    @Query("SELECT c FROM ChatEntity c LEFT JOIN FETCH c.messages WHERE c.id = :id")
    Optional<ChatEntity> findByIdWithMessages(@Param("id") Long id);
    
    /**
     * Подсчет количества чатов
     * @return общее количество чатов
     */
    @Query("SELECT COUNT(c) FROM ChatEntity c")
    long countAllChats();
    
    /**
     * Получение всех чатов, отсортированных по дате создания (новые первыми)
     * @param pageable параметры пагинации
     * @return страница чатов, отсортированных по дате создания
     */
    Page<ChatEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
}
