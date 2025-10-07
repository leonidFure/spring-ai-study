package com.example.springaiapp.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.NonNull;

import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.mappers.MessageMapperService;

import jakarta.transaction.Transactional;
import lombok.Builder;

// используем свою реализацию, т.к. в MessageWindowChatMemory нет разделения
// между тем, сколько сообщений хранить в памяти модели и сколько в базе данных
// т.е. если мы хотим хранить 2 сообщения в контексте модели, то в бд должно быть 2 сообщения
// а здесь мы можем просто селектнуть последние maxMessages сообщения из бд
@Builder
public class PostgresChatMemory implements ChatMemory {
    private final long maxMessages;
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
    @NonNull
    public List<Message> get(String conversationId) {
        return messageRepository.findLastMessageInChatTopN(Long.parseLong(conversationId), maxMessages)
                .stream()
                .map(it -> messageMapperService.toMessage(it))
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        messageRepository.deleteByChatId(Long.parseLong(conversationId));
    }
}
