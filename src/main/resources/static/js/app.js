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
    }
    
    // Отправка сообщения
    async function sendMessage() {
        const message = messageInput.value.trim();
        if (!message) return;
        
        // Добавляем сообщение пользователя в интерфейс
        addMessageToChat(message, 'user');
        
        // Очищаем поле ввода
        messageInput.value = '';
        
        // Показываем индикатор загрузки
        showLoadingIndicator();
        
        try {
            // Отправляем сообщение на сервер
            const response = await fetch('/api/messages', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    chatId: currentChatId,
                    content: message,
                    role: 'USER'
                })
            });
            
            if (!response.ok) {
                throw new Error('Ошибка при отправке сообщения');
            }
            
            const data = await response.json();
            
            // Добавляем ответ AI в интерфейс
            addMessageToChat(data.content, 'ai');
            
        } catch (error) {
            console.error('Ошибка:', error);
            addMessageToChat('Извините, произошла ошибка при обработке вашего сообщения.', 'ai');
        } finally {
            hideLoadingIndicator();
        }
    }
    
    // Добавление сообщения в чат
    function addMessageToChat(content, role) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${role}-message`;
        
        const avatar = '/images/placeholder-avatar.svg';
        const alt = role === 'user' ? 'Пользователь' : 'AI';
        
        messageDiv.innerHTML = `
            <div class="message-avatar">
                <img src="${avatar}" alt="${alt}" onerror="this.style.display='none'">
            </div>
            <div class="message-content">
                <div class="message-bubble ${role}-bubble">
                    <p>${escapeHtml(content)}</p>
                </div>
            </div>
        `;
        
        chatMessages.appendChild(messageDiv);
        scrollToBottom();
    }
    
    // Показать индикатор загрузки
    function showLoadingIndicator() {
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'message ai-message sending';
        loadingDiv.id = 'loadingMessage';
        
        loadingDiv.innerHTML = `
            <div class="message-avatar">
                <img src="/images/placeholder-avatar.svg" alt="AI">
            </div>
            <div class="message-content">
                <div class="message-bubble ai-bubble">
                    <p>Думаю...</p>
                </div>
            </div>
        `;
        
        chatMessages.appendChild(loadingDiv);
        scrollToBottom();
    }
    
    // Скрыть индикатор загрузки
    function hideLoadingIndicator() {
        const loadingMessage = document.getElementById('loadingMessage');
        if (loadingMessage) {
            loadingMessage.remove();
        }
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
        const chatList = document.querySelector('.chat-list');
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
        
        chatList.appendChild(chatItem);
    }
    
    // Выбор чата
    function selectChat(chatId) {
        // Убираем активный класс со всех чатов
        chatItems.forEach(item => item.classList.remove('active'));
        
        // Добавляем активный класс к выбранному чату
        const selectedChat = document.querySelector(`[data-chat-id="${chatId}"]`);
        if (selectedChat) {
            selectedChat.classList.add('active');
        }
        
        currentChatId = chatId;
        
        // Загружаем сообщения чата
        loadChatMessages(chatId);
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
            }
            
            // Если удаленный чат был активным, очищаем область сообщений
            if (currentChatId === deleteChatId) {
                currentChatId = null;
                chatMessages.innerHTML = '';
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
});
