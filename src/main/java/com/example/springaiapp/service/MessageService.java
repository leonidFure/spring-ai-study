package com.example.springaiapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.Generation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.springaiapp.api.dto.AiMessageResponse;
import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.api.mapper.MessageMapper;
import com.example.springaiapp.infrastracture.entity.Message;
import com.example.springaiapp.infrastracture.repository.ChatRepository;
import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.mappers.MessageMapperService;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Сервис для работы с сообщениями
 * Содержит бизнес-логику для управления сообщениями
 */
@Service
@RequiredArgsConstructor
public class MessageService {
    private static final String START_MESSAGE = "Начинаю обработку";
    private static final String USER_MESSAGE = "Сообщение пользователя сохранено";
    private static final String COMPLETE = "Ответ завершен";

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ChatRepository chatRepository;
    private final MessageMapperService messageMapperService;
    private final ChatClient chatClient;

    /**
     * Получение сообщения по ID
     * 
     * @param id идентификатор сообщения
     * @return сообщение или пустой Optional
     */
    public Optional<MessageDto> getMessageById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDto);
    }

    /**
     * Получение всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     * @return список сообщений в чате
     */
    public List<MessageDto> getMessagesByChatId(Long chatId) {
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
    public Page<MessageDto> getMessagesByChatId(Long chatId, int page, int size) {
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
    public List<MessageDto> searchMessagesByContent(String content) {
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
    public Optional<MessageDto> getLastMessageInChat(Long chatId) {
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
    public List<MessageDto> getMessagesAfterDate(Long chatId, LocalDateTime dateFrom) {
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
    public long getMessageCountInChat(Long chatId) {
        return messageRepository.countByChatId(chatId);
    }

    /**
     * Удаление всех сообщений в чате
     * 
     * @param chatId идентификатор чата
     */
    @Transactional
    public void deleteAllMessagesInChat(Long chatId) {
        messageRepository.deleteByChatId(chatId);
    }

    /**
     * Отправка сообщения (пока без AI интеграции)
     * 
     * @param request данные сообщения
     * @return ответное сообщение
     */
    @Transactional
    public MessageDto sendMessage(SendMessageRequest request) {
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
    @Transactional
    public Message createUserMessage(SendMessageRequest request) {
        return messageMapperService.createUserMessage(request);
    }

    /**
     * Сохранение сообщения
     * 
     * @param message сообщение для сохранения
     * @return сохраненное сообщение
     */
    @Transactional
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    /**
     * Создание сообщения ассистента
     * 
     * @param chatId  идентификатор чата
     * @param content содержимое сообщения
     * @return созданное сообщение ассистента
     */
    public Message createAssistantMessage(Long chatId, String content) {
        return messageMapperService.createAssistantMessage(chatId, content);
    }

    /**
     * Генерация потокового ответа (имитация)
     * 
     * @param request данные сообщения
     * @return сгенерированный ответ
     */
    @SneakyThrows
    @Transactional
    public SseEmitter generateStreamingResponse(SendMessageRequest request) {
        final var sseEmitter = new SseEmitter();
        sseEmitter.send(AiMessageResponse.start(START_MESSAGE));
        final var userMessage = messageMapperService.createUserMessage(request);
        messageRepository.save(userMessage);
        sseEmitter.send(AiMessageResponse.userMessage(USER_MESSAGE));
        final var messageBuilder = new StringBuilder();
        chatClient.prompt().user(request.getContent()).stream()
                .chatResponse()
                .subscribe(response -> processToken(sseEmitter, messageBuilder, response.getResult()),
                        sseEmitter::completeWithError,
                        () -> finishEmitt(request, sseEmitter, messageBuilder));
        return sseEmitter;
    }



    @SneakyThrows
    private void processToken(SseEmitter sseEmitter, StringBuilder messageBuilder, Generation result) {
        sseEmitter.send(AiMessageResponse.aiMessage(result.getOutput().getText()));
        messageBuilder.append(result.getOutput().getText());
    }

    @SneakyThrows
	private void finishEmitt(SendMessageRequest request, final SseEmitter sseEmitter, final StringBuilder messageBuilder) {
		final var assistantMessage = messageMapperService.createAssistantMessage(
		        request.getChatId(),
		        messageBuilder.toString());
		messageRepository.save(assistantMessage);
		sseEmitter.send(AiMessageResponse.complete(COMPLETE));
	}
}
