package com.example.springaiapp.api.mapper;

import com.example.springaiapp.api.dto.ChatDto;
import com.example.springaiapp.api.dto.CreateChatRequest;
import com.example.springaiapp.infrastracture.entity.ChatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Маппер для конвертации между Chat Entity и ChatDto
 */
@Mapper(componentModel = "spring")
public interface ChatMapper {
    
    /**
     * Конвертация Chat Entity в ChatDto
     * @param chat сущность чата
     * @return DTO чата
     */
    @Mapping(target = "messages", ignore = true)
    @Mapping(target = "messageCount", ignore = true)
    ChatDto toDto(ChatEntity chat);
    
    /**
     * Конвертация Chat Entity в ChatDto с сообщениями
     * @param chat сущность чата с сообщениями
     * @return DTO чата с сообщениями
     */
    @Mapping(target = "messageCount", expression = "java(chat.getMessages() != null ? (long) chat.getMessages().size() : 0L)")
    ChatDto toDtoWithMessages(ChatEntity chat);
    
    /**
     * Конвертация CreateChatRequest в Chat Entity
     * @param request запрос на создание чата
     * @return сущность чата
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "messages", ignore = true)
    ChatEntity toEntity(CreateChatRequest request);
    
    
    /**
     * Обновление Chat Entity из CreateChatRequest
     * @param request запрос на создание чата
     * @param chat существующая сущность чата
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "messages", ignore = true)
    void updateFromRequest(CreateChatRequest request, @MappingTarget ChatEntity chat);
}
