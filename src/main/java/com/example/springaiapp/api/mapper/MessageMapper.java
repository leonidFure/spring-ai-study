package com.example.springaiapp.api.mapper;

import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Маппер для конвертации между Message Entity и MessageDto
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {
    
    /**
     * Конвертация Message Entity в MessageDto
     * @param message сущность сообщения
     * @return DTO сообщения
     */
    MessageDto toDto(Message message);
    
    /**
     * Конвертация SendMessageRequest в Message Entity
     * @param request запрос на отправку сообщения
     * @return сущность сообщения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Message toEntity(SendMessageRequest request);
    
    
    /**
     * Обновление Message Entity из SendMessageRequest
     * @param request запрос на отправку сообщения
     * @param message существующая сущность сообщения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    void updateFromRequest(SendMessageRequest request, @MappingTarget Message message);
    
    /**
     * Обновление содержимого сообщения
     * @param content новое содержимое
     * @param message существующая сущность сообщения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chatId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    void updateContent(String content, @MappingTarget Message message);
}
