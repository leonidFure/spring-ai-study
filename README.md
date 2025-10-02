# Spring AI Chat Application

Веб-приложение для чата с искусственным интеллектом, построенное на Spring Boot 3.2, Spring AI и Ollama.

## Технологии

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring AI 1.0.0-M4** - интеграция с AI моделями
- **Ollama** - локальный сервер для запуска AI моделей
- **PostgreSQL** - база данных для хранения истории чата
- **Thymeleaf** - шаблонизатор для веб-интерфейса
- **Maven** - система сборки

## Предварительные требования

1. **Java 21** или выше
2. **Maven 3.6+**
3. **PostgreSQL 12+**
4. **Docker** (для запуска Ollama)

## Установка и запуск

### 1. Запуск Ollama с помощью Docker

```bash
# Запуск Ollama с предустановленными моделями
docker-compose up -d

# Проверка, что Ollama запущен
curl http://localhost:11434/api/tags
```

### 2. Настройка базы данных PostgreSQL

```sql
-- Создание базы данных
CREATE DATABASE spring_ai_db;

-- Создание пользователя (опционально)
CREATE USER spring_user WITH PASSWORD 'spring_password';
GRANT ALL PRIVILEGES ON DATABASE spring_ai_db TO spring_user;
```

### 3. Настройка приложения

Отредактируйте `src/main/resources/application.properties` при необходимости:

```properties
# Настройки базы данных
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_ai_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Настройки Ollama
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.model=gemma3:4b-it-qat
spring.ai.ollama.embedding.model=mxbai-embed-large:335m
```

### 4. Сборка и запуск приложения

```bash
# Сборка проекта
mvn clean compile

# Запуск приложения
mvn spring-boot:run
```

Или через IDE:
- Запустите класс `SpringAiAppApplication`

### 5. Доступ к приложению

Откройте браузер и перейдите по адресу: http://localhost:8080

## Использование

1. **Веб-интерфейс**: Откройте http://localhost:8080 в браузере
2. **Введите сообщение** в поле ввода и нажмите "Отправить" или Enter
3. **Получите ответ** от AI модели
4. **Просматривайте историю** всех сообщений
5. **Очищайте историю** при необходимости

## API Endpoints

- `GET /` - перенаправление на чат
- `GET /chat` - веб-интерфейс чата
- `POST /chat/send` - отправка сообщения
- `GET /chat/history` - получение истории сообщений
- `POST /chat/clear` - очистка истории

## Структура проекта

```
src/
├── main/
│   ├── java/com/example/springaiapp/
│   │   ├── SpringAiAppApplication.java    # Главный класс приложения
│   │   ├── controller/                    # Веб-контроллеры
│   │   │   ├── ChatController.java
│   │   │   └── HomeController.java
│   │   ├── service/                       # Бизнес-логика
│   │   │   └── ChatService.java
│   │   ├── repository/                    # Репозитории для работы с БД
│   │   │   └── MessageRepository.java
│   │   └── model/                         # Сущности данных
│   │       └── Message.java
│   └── resources/
│       ├── application.properties         # Конфигурация приложения
│       └── templates/
│           └── chat.html                  # Шаблон веб-интерфейса
└── test/                                  # Тесты
```

## Настройка AI моделей

Приложение использует следующие модели Ollama:
- **gemma3:4b-it-qat** - для генерации ответов в чате
- **mxbai-embed-large:335m** - для создания эмбеддингов

Модели автоматически загружаются при запуске Docker контейнера.

## Возможные проблемы

1. **Ollama не запускается**: Убедитесь, что Docker запущен и порт 11434 свободен
2. **Ошибка подключения к БД**: Проверьте настройки PostgreSQL и убедитесь, что база данных создана
3. **Модели не загружаются**: Проверьте логи Docker контейнера Ollama

## Разработка

Для разработки рекомендуется использовать IDE с поддержкой Spring Boot:
- IntelliJ IDEA
- Eclipse с Spring Tools
- Visual Studio Code с расширениями для Java и Spring

## Лицензия

MIT License
