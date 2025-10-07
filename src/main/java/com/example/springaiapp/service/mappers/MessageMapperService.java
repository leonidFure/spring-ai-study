package com.example.springaiapp.service.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.Message;

@Service
public class MessageMapperService {

    public Message createUserMessage(SendMessageRequest request) {
        final var message = new Message();
        message.setChatId(request.getChatId());
        message.setContent(request.getContent());
        message.setRole(Message.MessageRole.USER);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

    public Message createAssistantMessage(Long chatId, String content) {
        final var message = new Message();
        message.setChatId(chatId);
        message.setContent(content);
        message.setRole(Message.MessageRole.ASSISTANT);
        message.setCreatedAt(LocalDateTime.now());
        return message;
    }

}
