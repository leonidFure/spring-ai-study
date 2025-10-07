package com.example.springaiapp.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.springaiapp.api.dto.ResponeMessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.MessageEntity;

/**
 * Маппер для конвертации между Message Entity и MessageDto
/**
 * Маппер для конвертации между MessageEntity и MessageDto
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    /**
     * Конвертация MessageEntity в ResponeMessageDto
     * @param message сущность сообщения
     * @return DTO сообщения
     */
    ResponeMessageDto toDto(MessageEntity message);
    
    /**
     * Конвертация SendMessageRequest в Message Entity
     * @param request запрос на отправку сообщения
     * @return сущность сообщения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    MessageEntity toEntity(SendMessageRequest request);
    
    /**
     * Обновление Message Entity из SendMessageRequest
     * @param request запрос на отправку сообщения
     * @param message существующая сущность сообщения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "chat", ignore = true)
    void updateFromRequest(SendMessageRequest request, @MappingTarget MessageEntity message);
    
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
    void updateContent(String content, @MappingTarget MessageEntity message);
}
