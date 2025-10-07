package com.example.springaiapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.springaiapp.api.dto.AiMessageResponse;
import com.example.springaiapp.api.dto.ResponeMessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.api.mapper.MessageMapper;
import com.example.springaiapp.infrastracture.entity.MessageEntity;
import com.example.springaiapp.infrastracture.repository.ChatRepository;
import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.MessageService;
import com.example.springaiapp.service.mappers.MessageMapperService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация сервиса для работы с сообщениями
 * Содержит бизнес-логику для управления сообщениями
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private static final String START_MESSAGE = "Начинаю обработку";
    private static final String COMPLETE = "Ответ завершен";

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatRepository chatRepository;
    private final MessageMapperService messageMapperService;
    private final ChatClient chatClient;
    @Qualifier("postgresChatMemory")
    private final ChatMemory postgresChatMemory;

    /**
     * Получение сообщения по ID
     * 
     * @param id идентификатор сообщения
     * @return сообщение или пустой Optional
     */
    @Override
    public Optional<ResponeMessageDto> getMessageById(final Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDto);
    }

    /**
     * Получение всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     * @return список сообщений в чате
     */
    @Override
    public List<ResponeMessageDto> getMessagesByChatId(final Long chatId) {
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение сообщений в чате с пагинацией
     * 
     * @param chatId идентификатор чата
     * @param page   номер страницы
     * @param size   размер страницы
     * @return страница сообщений
     */
    @Override
    public Page<ResponeMessageDto> getMessagesByChatId(final Long chatId, final int page, final int size) {
        final var pageable = PageRequest.of(page, size);
        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId, pageable)
                .map(messageMapper::toDto);
    }

    /**
     * Поиск сообщений по содержимому
     * 
     * @param content содержимое для поиска
     * @return список найденных сообщений
     */
    @Override
    public List<ResponeMessageDto> searchMessagesByContent(final String content) {
        return messageRepository.findByContentContainingIgnoreCase(content).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение последнего сообщения в чате
     * 
     * @param chatId идентификатор чата
     * @return последнее сообщение или пустой Optional
     */
    @Override
    public Optional<ResponeMessageDto> getLastMessageInChat(final Long chatId) {
        final var messages = messageRepository.findLastMessageInChat(chatId);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messageMapper.toDto(messages.get(0)));
    }

    /**
     * Получение сообщений, созданных после указанной даты в чате
     * 
     * @param chatId   идентификатор чата
     * @param dateFrom дата начала периода
     * @return список сообщений, созданных после указанной даты
     */
    @Override
    public List<ResponeMessageDto> getMessagesAfterDate(final Long chatId, final LocalDateTime dateFrom) {
        return messageRepository.findByChatIdAndCreatedAtAfterOrderByCreatedAtAsc(chatId, dateFrom).stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение количества сообщений в чате
     * 
     * @param chatId идентификатор чата
     * @return количество сообщений в чате
     */
    @Override
    public long getMessageCountInChat(final Long chatId) {
        return messageRepository.countByChatId(chatId);
    }

    /**
     * Удаление всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     */
    @Override
    @Transactional
    public void deleteAllMessagesInChat(final Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }

    /**
     * Отправка сообщения (пока без AI интеграции)
     * 
     * @param request данные сообщения
     * @return ответное сообщение
     */
    @Override
    @Transactional
    public ResponeMessageDto sendMessage(final SendMessageRequest request) {
        // Проверяем существование чата
        if (!chatRepository.existsById(request.getChatId())) {
            throw new IllegalArgumentException("Чат не найден");
        }

        // Создаем сообщение пользователя
        final var userMessage = messageMapperService.createUserMessage(request);
        messageRepository.save(userMessage);
        final var assistantResponse = chatClient.prompt().user(request.getContent()).call().content();
        final var assistantMessage = messageMapperService.createAssistantMessage(request.getChatId(),
                assistantResponse);
        final var savedAiMessage = messageRepository.save(assistantMessage);

        return messageMapper.toDto(savedAiMessage);
    }

    /**
     * Создание сообщения пользователя
     * 
     * @param request данные сообщения
     * @return созданное сообщение пользователя
     */
    @Override
    @Transactional
    public MessageEntity createUserMessage(final SendMessageRequest request) {
        return messageMapperService.createUserMessage(request);
    }

    /**
     * Сохранение сообщения
     * 
     * @param message сообщение для сохранения
     * @return сохраненное сообщение
     */
    @Override
    @Transactional
    public MessageEntity saveMessage(final MessageEntity message) {
        return messageRepository.save(message);
    }

    /**
     * Создание сообщения ассистента
     * 
     * @param chatId  идентификатор чата
     * @param content содержимое сообщения
     * @return созданное сообщение ассистента
     */
    @Override
    public MessageEntity createAssistantMessage(final Long chatId, final String content) {
        return messageMapperService.createAssistantMessage(chatId, content);
    }

    /**
     * Генерация потокового ответа (имитация)
     * 
     * @param request данные сообщения
     * @return сгенерированный ответ
     */
    @Override
    @SneakyThrows
    @Transactional
    public SseEmitter generateStreamingResponse(final SendMessageRequest request) {
        final var sseEmitter = new SseEmitter();
        sendEvent(sseEmitter, AiMessageResponse.start(START_MESSAGE));
        final var messageBuilder = new StringBuilder();
        chatClient.prompt()
                // отправляем сообщение пользователя в ollama model
                .user(request.getContent())
                // подкладываем идентификатор чата chatMemoryAdvisor, чтобы не создавать каждый
                // раз новый chatMemoryAdvisor
                // сам chatMemoryAdvisor создается один раз в SpringAiAppApplication и
                // используется для всех запросов
                .advisors(it -> it.param(ChatMemory.CONVERSATION_ID, String.valueOf(request.getChatId())))
                .stream()
                .chatResponse()
                // при получении токена отправляем его в SSE
                .subscribe(response -> processToken(sseEmitter, messageBuilder, response.getResult()),
                        // при ошибке отправляем ошибку в SSE
                        sseEmitter::completeWithError,
                        // при завершении отправляем сообщение о завершении в SSE
                        () -> sendEvent(sseEmitter, AiMessageResponse.complete(COMPLETE)));
        return sseEmitter;
    }

    @SneakyThrows
    private void processToken(final SseEmitter sseEmitter, final StringBuilder messageBuilder,
            final Generation result) {
        sseEmitter.send(AiMessageResponse.aiMessage(result.getOutput().getText()));
        messageBuilder.append(result.getOutput().getText());
    }

    @SneakyThrows
    private void sendEvent(final SseEmitter sseEmitter,
            final AiMessageResponse aiMessageResponse) {
        sseEmitter.send(aiMessageResponse);
    }
}
