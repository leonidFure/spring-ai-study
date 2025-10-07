package com.example.springaiapp.service.mappers;

import java.time.LocalDateTime;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.MessageEntity;

@Service
public class MessageMapperService {

    public MessageEntity createUserMessage(SendMessageRequest request) {
        final var message = new MessageEntity();
        message.setChatId(request.getChatId());
        message.setContent(request.getContent());
        message.setRole(MessageType.USER);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    public MessageEntity createAssistantMessage(Long chatId, String content) {
        final var message = new MessageEntity();
        message.setChatId(chatId);
        message.setContent(content);
        message.setRole(MessageType.ASSISTANT);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    public MessageEntity toEntity(Message message, Long chatId) {
        final var entity = new MessageEntity();
        entity.setChatId(chatId);
        entity.setRole(message.getMessageType());
        entity.setContent(message.getText());
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    public Message toMessage(MessageEntity entity) {
        return switch (entity.getRole()) {
            case USER -> new UserMessage(entity.getContent());
            case ASSISTANT -> new AssistantMessage(entity.getContent());
            case SYSTEM -> new SystemMessage(entity.getContent());
            default -> throw new IllegalArgumentException("Invalid message type: " + entity.getRole());
        };
    }

}
