package com.example.springaiapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Базовые тесты для Spring Boot приложения
 */
@ActiveProfiles("test")
@SpringBootTest
class SpringAiAppApplicationTests {

    @Test
    void contextLoads() {
        // Тест проверяет, что контекст Spring Boot загружается корректно
    }

}
