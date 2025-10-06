package com.example.springaiapp;

import com.example.springaiapp.api.dto.MessageDto;
import com.example.springaiapp.api.dto.SendMessageRequest;
import com.example.springaiapp.infrastracture.entity.Chat;
import com.example.springaiapp.infrastracture.entity.Message;
import com.example.springaiapp.infrastracture.repository.ChatRepository;
import com.example.springaiapp.infrastracture.repository.MessageRepository;
import com.example.springaiapp.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    public void testSaveAndLoadMessage() {
        // Создаем тестовый чат
        Chat chat = new Chat();
        chat.setTitle("Test Chat");
        Chat savedChat = chatRepository.save(chat);

        // Создаем запрос на отправку сообщения
        SendMessageRequest request = new SendMessageRequest();
        request.setChatId(savedChat.getId());
        request.setContent("Test message");
        request.setRole(Message.MessageRole.USER);

        // Отправляем сообщение
        MessageDto response = messageService.sendMessage(request);

        // Проверяем, что ответ AI сохранился
        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.getContent().contains("Test message"));
        assertEquals(Message.MessageRole.ASSISTANT, response.getRole());
        assertEquals(savedChat.getId(), response.getChatId());

        // Проверяем, что сообщение загружается из базы
        List<MessageDto> messages = messageService.getMessagesByChatId(savedChat.getId());
        assertEquals(2, messages.size()); // USER сообщение + ASSISTANT ответ

        // Проверяем, что сообщения сохранились в базе
        List<Message> dbMessages = messageRepository.findByChatIdOrderByCreatedAtAsc(savedChat.getId());
        assertEquals(2, dbMessages.size());
    }
}
