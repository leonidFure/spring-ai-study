package com.example.springaiapp.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.mappers.MessageMapperService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresChatMemory implements ChatMemory {
    private final MessageRepository messageRepository;
    private final MessageMapperService messageMapperService;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        messageRepository.saveAll(messages.stream()
                .map(it -> messageMapperService.toEntity(it, Long.parseLong(conversationId)))
                .collect(Collectors.toList()));
    }

    @Override
    public void clear(String conversationId) {
        messageRepository.deleteByChatId(Long.parseLong(conversationId));
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        return messageRepository.findLastMessageInChatTopN(Long.parseLong(conversationId), lastN)
                .stream()
                .map(it -> messageMapperService.toMessage(it))
                .collect(Collectors.toList());
    }
}
