// Основной JavaScript для чат-интерфейса
document.addEventListener('DOMContentLoaded', function() {
    // Элементы интерфейса
    const messageInput = document.getElementById('messageInput');
    const sendButton = document.getElementById('sendMessageBtn');
    const chatMessages = document.getElementById('chatMessages');
    const newChatInput = document.getElementById('newChatTitle');
    const createChatBtn = document.getElementById('createChatBtn');
    const deleteModal = document.getElementById('deleteModal');
    const chatItems = document.querySelectorAll('.chat-item');
    const topicItems = document.querySelectorAll('.topic-item');
    
    // Текущий активный чат
    let currentChatId = null;
    let deleteChatId = null;
    
    // Инициализация
    init();
    
    function init() {
        // Отладочная информация
        console.log('Инициализация приложения с темной темой');
        console.log('window.lastChatId:', window.lastChatId);
        console.log('currentChatId до инициализации:', currentChatId);
        
        // Инициализируем темную тему
        initDarkTheme();
        
        // Проверяем, что все необходимые элементы найдены
        if (!messageInput || !sendButton || !chatMessages) {
            console.error('Не найдены необходимые элементы DOM');
            return;
        }
        
        // Обработчики событий для отправки сообщений
        sendButton.addEventListener('click', sendMessage);
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
        
        // Обработчики для создания нового чата
        createChatBtn.addEventListener('click', createNewChat);
        newChatInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                createNewChat();
            }
        });
        
        // Обработчики для чатов
        chatItems.forEach(item => {
            item.addEventListener('click', function() {
                const chatId = this.dataset.chatId;
                selectChat(chatId);
            });
            
            const deleteBtn = item.querySelector('.chat-delete');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    const chatId = this.dataset.chatId;
                    showDeleteModal(chatId);
                });
            }
        });
        
        // Обработчики для тем
        topicItems.forEach(item => {
            item.addEventListener('click', function() {
                selectTopic(this);
            });
            
            const closeBtn = item.querySelector('.topic-close');
            if (closeBtn) {
                closeBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    removeTopic(this.parentElement);
                });
            }
        });
        
        // Обработчики модального окна
        const cancelBtn = deleteModal.querySelector('.btn-cancel');
        const confirmBtn = deleteModal.querySelector('.btn-confirm');
        
        cancelBtn.addEventListener('click', hideDeleteModal);
        confirmBtn.addEventListener('click', confirmDeleteChat);
        
        // Закрытие модального окна по клику вне его
        deleteModal.addEventListener('click', function(e) {
            if (e.target === deleteModal) {
                hideDeleteModal();
            }
        });
        
        // Автофокус на поле ввода
        messageInput.focus();
        
        // Автоматически выбираем последний чат при загрузке (после инициализации обработчиков)
        if (window.lastChatId) {
            console.log('Автоматически выбираем последний чат:', window.lastChatId);
            // Используем setTimeout чтобы убедиться, что DOM полностью загружен
            setTimeout(() => {
                selectChat(window.lastChatId);
            }, 100);
        } else {
            console.log('lastChatId не передан, проверяем наличие чатов в DOM');
            // Если lastChatId не передан, попробуем выбрать первый доступный чат
            setTimeout(() => {
                const firstChat = document.querySelector('.chat-item');
                if (firstChat) {
                    const chatId = firstChat.dataset.chatId;
                    console.log('Выбираем первый доступный чат:', chatId);
                    selectChat(chatId);
                } else {
                    console.log('Чатов в DOM не найдено');
                }
            }, 100);
        }
    }
    
    // Отправка сообщения
    async function sendMessage() {
        const message = messageInput.value.trim();
        if (!message) return;
        
        // Проверяем, что выбран чат
        if (!currentChatId) {
            console.error('Не выбран чат для отправки сообщения');
            alert('Пожалуйста, выберите чат для отправки сообщения');
            return;
        }
        
        // Добавляем сообщение пользователя в интерфейс
        addMessageToChat(message, 'user');
        
        // Очищаем поле ввода
        messageInput.value = '';
        
        try {
            // Используем стриминговый API для получения ответа
            await sendStreamingMessage(message);
            
        } catch (error) {
            console.error('Ошибка:', error);
            addMessageToChat('Извините, произошла ошибка при обработке вашего сообщения.', 'ai');
        }
    }
    
    // Отправка стримингового сообщения через SSE
    async function sendStreamingMessage(message) {
        return new Promise((resolve, reject) => {
            // Создаем контейнер для стримингового ответа
            const streamingMessageDiv = addMessageToChat('', 'ai');
            const messageBubble = streamingMessageDiv.querySelector('.message-text');
            
            // Проверяем, что messageBubble найден
            if (!messageBubble) {
                console.error('Не удалось найти элемент .message-text в созданном сообщении');
                reject(new Error('Ошибка создания элемента сообщения'));
                return;
            }
            
            // Отправляем POST запрос для инициации стриминга
            fetch('/api/stream/messages', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    chatId: currentChatId,
                    content: message,
                    role: 'USER'
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при инициации стриминга');
                }
                
                // Получаем поток данных
                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let buffer = '';
                
                // Функция для чтения потока
                function readStream() {
                    return reader.read().then(({ done, value }) => {
                        if (done) {
                            console.log('Стриминг завершен');
                            resolve();
                            return;
                        }
                        
                        // Декодируем полученные данные
                        buffer += decoder.decode(value, { stream: true });
                        
                        // Обрабатываем события SSE
                        const lines = buffer.split('\n');
                        buffer = lines.pop(); // Оставляем неполную строку в буфере
                        
                        for (const line of lines) {
                            console.log('Обработка строки:', line);
                            if (line.trim() === '') continue;
                            
                            if (line.startsWith('data:')) {
                                const data = line.substring(5).trim();
                                handleSSEEvent(data, messageBubble);
                            }
                        }
                        
                        // Продолжаем чтение
                        return readStream();
                    });
                }
                
                // Функция обработки SSE событий
                function handleSSEEvent(data, messageElement) {
                    // Проверяем, что messageElement существует
                    if (!messageElement) {
                        console.error('messageElement равен null, пропускаем обработку события');
                        return;
                    }
                    
                    try {
                        // Парсим JSON объект AiMessageResponse
                        const aiResponse = JSON.parse(data);
                        console.log('Получен AiMessageResponse:', aiResponse);
                        
                        // Находим контейнер сообщения и элемент статуса
                        const messageContainer = messageElement.closest('.message-bubble');
                        const statusElement = messageContainer ? messageContainer.querySelector('.message-status') : null;
                        
                        // Обрабатываем в зависимости от типа сообщения
                        switch (aiResponse.messageType) {
                            case 'start':
                                console.log('Начинаю обработку:', aiResponse.message);
                                // Показываем статус
                                if (statusElement) {
                                    statusElement.textContent = aiResponse.message;
                                    statusElement.style.display = 'block';
                                }
                                break;
                                
                            case 'user_message':
                                console.log('Сообщение пользователя сохранено:', aiResponse.message);
                                // Обновляем статус
                                if (statusElement) {
                                    statusElement.textContent = aiResponse.message;
                                    statusElement.style.display = 'block';
                                }
                                break;
                                
                            case 'ai_message':
                                // Добавляем новый фрагмент к существующему тексту
                                messageElement.textContent += aiResponse.message;
                                // Прокручиваем к низу для отображения нового контента
                                scrollToBottom();
                                break;
                                
                            case 'complete':
                                console.log('Ответ завершен:', aiResponse.message);
                                // Обновляем статус
                                if (statusElement) {
                                    statusElement.textContent = aiResponse.message;
                                    statusElement.style.display = 'block';
                                }
                                break;
                                
                            default:
                                console.warn('Неизвестный тип сообщения:', aiResponse.messageType);
                                // Добавляем как обычный текст
                                messageElement.textContent += aiResponse.message;
                                scrollToBottom();
                                break;
                        }
                    } catch (error) {
                        console.error('Ошибка при парсинге JSON:', error, 'Данные:', data);
                        // Если не удалось распарсить JSON, обрабатываем как обычный текст
                        if (messageElement) {
                            messageElement.textContent += data;
                            scrollToBottom();
                        }
                    }
                }
                
                // Начинаем чтение потока
                readStream().catch(error => {
                    console.error('Ошибка при чтении потока:', error);
                    if (messageBubble) {
                        messageBubble.textContent = 'Ошибка при получении ответа. Попробуйте еще раз.';
                    }
                    reject(error);
                });
                
            })
            .catch(error => {
                console.error('Ошибка при отправке запроса:', error);
                if (messageBubble) {
                    messageBubble.textContent = 'Ошибка при отправке сообщения. Попробуйте еще раз.';
                }
                reject(error);
            });
        });
    }
    
    
    // Добавление сообщения в чат
    function addMessageToChat(content, role) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${role}-message`;
        
        const currentTime = new Date().toLocaleTimeString('ru-RU', { 
            hour: '2-digit', 
            minute: '2-digit' 
        });
        
        const author = role === 'user' ? 'Вы' : 'ИИ Ассистент';
        const avatarIcon = role === 'user' ? '👤' : '🤖';
        
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <div class="avatar-icon">${avatarIcon}</div>
            </div>
            <div class="message-content">
                <div class="message-header">
                    <span class="message-author">${author}</span>
                    <span class="message-time">${currentTime}</span>
                </div>
                <div class="message-bubble ${role}-bubble">
                    <div class="message-status" style="display: none; color: var(--text-muted); font-size: 11px; margin-bottom: 4px; font-style: italic;"></div>
                    <div class="message-text">${escapeHtml(content)}</div>
                </div>
            </div>
        `;
        
        chatMessages.appendChild(messageDiv);
        scrollToBottom();
        
        return messageDiv;
    }
    
    
    // Создание нового чата
    async function createNewChat() {
        const title = newChatInput.value.trim();
        if (!title) {
            alert('Пожалуйста, введите название чата');
            return;
        }
        
        try {
            const response = await fetch('/api/chats', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    title: title
                })
            });
            
            if (!response.ok) {
                throw new Error('Ошибка при создании чата');
            }
            
            const data = await response.json();
            
            // Добавляем новый чат в список
            addChatToList(data);
            
            // Выбираем новый чат
            selectChat(data.id);
            
            // Очищаем поле ввода
            newChatInput.value = '';
            
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Ошибка при создании чата: ' + error.message);
        }
    }
    
    // Добавление чата в список
    function addChatToList(chat) {
        const chatItems = document.querySelector('.chat-items');
        const chatItem = document.createElement('div');
        chatItem.className = 'chat-item';
        chatItem.dataset.chatId = chat.id;
        
        chatItem.innerHTML = `
            <span class="chat-name">${escapeHtml(chat.title)}</span>
            <button class="chat-delete" type="button" data-chat-id="${chat.id}">×</button>
        `;
        
        // Добавляем обработчики событий
        chatItem.addEventListener('click', function() {
            selectChat(chat.id);
        });
        
        const deleteBtn = chatItem.querySelector('.chat-delete');
        deleteBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            showDeleteModal(chat.id);
        });
        
        chatItems.appendChild(chatItem);
        
        // Обновляем счетчик чатов
        updateChatCount();
    }
    
    // Обновление счетчика чатов
    function updateChatCount() {
        const chatCount = document.querySelector('.chat-count');
        const chatItems = document.querySelectorAll('.chat-item');
        if (chatCount) {
            chatCount.textContent = chatItems.length;
        }
    }
    
    // Выбор чата
    function selectChat(chatId) {
        console.log('Выбираем чат с ID:', chatId);
        
        // Убираем активный класс со всех чатов
        document.querySelectorAll('.chat-item').forEach(item => item.classList.remove('active'));
        
        // Добавляем активный класс к выбранному чату
        const selectedChat = document.querySelector(`[data-chat-id="${chatId}"]`);
        if (selectedChat) {
            selectedChat.classList.add('active');
            console.log('Чат найден и активирован');
            
            // Обновляем заголовок чата
            updateChatHeader(selectedChat.querySelector('.chat-name').textContent);
        } else {
            console.warn('Чат с ID', chatId, 'не найден в DOM');
            updateChatHeader('Выберите чат для начала общения');
        }
        
        currentChatId = chatId;
        console.log('currentChatId установлен в:', currentChatId);
        
        // Загружаем сообщения чата
        loadChatMessages(chatId);
    }
    
    // Обновление заголовка чата
    function updateChatHeader(chatTitle) {
        const chatTitleElement = document.getElementById('currentChatTitle');
        if (chatTitleElement) {
            chatTitleElement.textContent = chatTitle;
        }
    }
    
    // Загрузка сообщений чата
    async function loadChatMessages(chatId) {
        try {
            const response = await fetch(`/api/messages/chat/${chatId}`);
            if (!response.ok) {
                throw new Error('Ошибка при загрузке сообщений');
            }
            
            const data = await response.json();
            
            // Очищаем область сообщений
            chatMessages.innerHTML = '';
            
            // Добавляем сообщения
            data.forEach(message => {
                addMessageToChat(message.content, message.role.toLowerCase());
            });
            
        } catch (error) {
            console.error('Ошибка:', error);
            chatMessages.innerHTML = '<p>Ошибка при загрузке сообщений</p>';
        }
    }
    
    // Выбор темы
    function selectTopic(topicElement) {
        // Убираем активный класс со всех тем
        topicItems.forEach(item => item.classList.remove('active'));
        
        // Добавляем активный класс к выбранной теме
        topicElement.classList.add('active');
    }
    
    // Удаление темы
    function removeTopic(topicElement) {
        topicElement.remove();
    }
    
    // Показать модальное окно удаления
    function showDeleteModal(chatId) {
        deleteChatId = chatId;
        deleteModal.classList.add('show');
    }
    
    // Скрыть модальное окно удаления
    function hideDeleteModal() {
        deleteModal.classList.remove('show');
        deleteChatId = null;
    }
    
    // Подтверждение удаления чата
    async function confirmDeleteChat() {
        if (!deleteChatId) return;
        
        try {
            const response = await fetch(`/api/chats/${deleteChatId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error('Ошибка при удалении чата');
            }
            
            // Удаляем чат из списка
            const chatItem = document.querySelector(`[data-chat-id="${deleteChatId}"]`);
            if (chatItem) {
                chatItem.remove();
                updateChatCount();
            }
            
            // Если удаленный чат был активным, очищаем область сообщений
            if (currentChatId === deleteChatId) {
                currentChatId = null;
                chatMessages.innerHTML = '';
                updateChatHeader('Выберите чат для начала общения');
            }
            
            hideDeleteModal();
            
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Ошибка при удалении чата: ' + error.message);
        }
    }
    
    // Прокрутка к низу чата
    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // Экранирование HTML
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    // Обработка ошибок
    window.addEventListener('error', function(e) {
        console.error('Глобальная ошибка:', e.error);
    });
    
    // Обработка необработанных промисов
    window.addEventListener('unhandledrejection', function(e) {
        console.error('Необработанная ошибка промиса:', e.reason);
    });
    
    // Инициализация темной темы
    function initDarkTheme() {
        // Устанавливаем темную тему для браузера
        document.documentElement.style.colorScheme = 'dark';
        
        // Добавляем класс для темной темы к body
        document.body.classList.add('dark-theme');
        
        // Настраиваем скроллбары для темной темы
        const style = document.createElement('style');
        style.textContent = `
            ::-webkit-scrollbar {
                width: 8px;
            }
            ::-webkit-scrollbar-track {
                background: var(--bg-tertiary);
                border-radius: 4px;
            }
            ::-webkit-scrollbar-thumb {
                background: var(--border-secondary);
                border-radius: 4px;
            }
            ::-webkit-scrollbar-thumb:hover {
                background: var(--text-muted);
            }
        `;
        document.head.appendChild(style);
        
        console.log('Темная тема инициализирована');
    }
});
