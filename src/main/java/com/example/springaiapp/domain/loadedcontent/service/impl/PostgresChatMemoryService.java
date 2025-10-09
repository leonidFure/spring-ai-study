package com.example.springaiapp.domain.loadedcontent.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.lang.NonNull;

import com.example.springaiapp.infrastracture.repository.MessageRepository;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

// используем свою реализацию, т.к. в MessageWindowChatMemory нет разделения
// между тем, сколько сообщений хранить в памяти модели и сколько в базе данных
// т.е. если мы хотим хранить 2 сообщения в контексте модели, то в бд должно быть 2 сообщения
// а здесь мы можем просто селектнуть последние maxMessages сообщения из бд
@Slf4j
@Builder
public class PostgresChatMemoryService implements ChatMemory {
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
        // берем последние maxMessages сообщений из бд
        // отсортированных по дате создания
        // начиная со старых сообщений и заканчивая новыми
        // нужно для того, чтобы в модель передавался правильный порядок сообщений
        // пример, если мы просим каждый раз отдавать нам число на 1 больше чем мы отдали,
        // у модели будет история 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        // если мы будем просто сортировать по desc, то модели будет история 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 (в обратном порядке)
        final var count = messageRepository.countByChatId(Long.parseLong(conversationId));
        return messageRepository
                .findNewestMessagesInChatTopN(Long.parseLong(conversationId),
                        count > maxMessages ? count - maxMessages : 0)
                .stream()
                .map(it -> messageMapperService.toMessage(it))
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        messageRepository.deleteByChatId(Long.parseLong(conversationId));
    }
}
