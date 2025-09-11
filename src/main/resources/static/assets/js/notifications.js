// Система уведомлений с WebSocket
class NotificationManager {
    constructor() {
        this.stompClient = null;
        this.connected = false;
        this.notifications = [];
        this.maxNotifications = 10;
        this.init();
    }

    init() {
        this.connectWebSocket();
        this.createNotificationContainer();
        this.setupNotificationSound();
    }

    connectWebSocket() {
        const socket = new SockJS('/ws-endpoint');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket подключен: ' + frame);
            this.connected = true;
            
            // Подписываемся на уведомления
            this.stompClient.subscribe('/topic/notifications', (message) => {
                const notification = JSON.parse(message.body);
                this.showNotification(notification);
            });
            
            // Подписываемся на обновления заказов
            this.stompClient.subscribe('/topic/orders', (message) => {
                const update = JSON.parse(message.body);
                this.handleOrderUpdate(update);
            });
            
        }, (error) => {
            console.error('Ошибка подключения WebSocket:', error);
            this.connected = false;
            // Пытаемся переподключиться через 5 секунд
            setTimeout(() => this.connectWebSocket(), 5000);
        });
    }

    createNotificationContainer() {
        // Создаем контейнер для уведомлений, если его нет
        if (!document.getElementById('notification-container')) {
            const container = document.createElement('div');
            container.id = 'notification-container';
            container.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 9999;
                max-width: 400px;
                pointer-events: none;
            `;
            document.body.appendChild(container);
        }
    }

    setupNotificationSound() {
        // Создаем аудио элемент для звуковых уведомлений
        this.notificationSound = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBSuBzvLZiTYIG2m98OScTgwOUarm7blmGgU7k9n1unEiBC13yO/eizEIHWq+8+OWT');
    }

    showNotification(notification) {
        // Добавляем уведомление в массив
        this.notifications.unshift(notification);
        if (this.notifications.length > this.maxNotifications) {
            this.notifications.pop();
        }

        // Создаем элемент уведомления
        const notificationElement = this.createNotificationElement(notification);
        
        // Добавляем в контейнер
        const container = document.getElementById('notification-container');
        container.appendChild(notificationElement);

        // Показываем уведомление
        setTimeout(() => {
            notificationElement.style.transform = 'translateX(0)';
            notificationElement.style.opacity = '1';
        }, 100);

        // Воспроизводим звук
        this.playNotificationSound();

        // Показываем браузерное уведомление
        this.showBrowserNotification(notification);

        // Автоматически скрываем через 5 секунд
        setTimeout(() => {
            this.hideNotification(notificationElement);
        }, 5000);

        // Обновляем счетчик уведомлений в навигации
        this.updateNotificationCounter();
    }

    createNotificationElement(notification) {
        const element = document.createElement('div');
        element.className = 'notification-item';
        element.style.cssText = `
            background: white;
            border-left: 4px solid ${this.getColorForType(notification.color)};
            border-radius: 4px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            margin-bottom: 10px;
            padding: 15px;
            transform: translateX(100%);
            opacity: 0;
            transition: all 0.3s ease;
            pointer-events: auto;
            max-width: 350px;
        `;

        const icon = this.getIconForType(notification.icon);
        const time = notification.timestamp || new Date().toLocaleTimeString();

        element.innerHTML = `
            <div style="display: flex; align-items: flex-start; justify-content: space-between;">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; margin-bottom: 5px;">
                        <i class="fas fa-${icon}" style="color: ${this.getColorForType(notification.color)}; margin-right: 8px; font-size: 16px;"></i>
                        <strong style="font-size: 14px; color: #333;">${notification.title}</strong>
                    </div>
                    <p style="margin: 0; font-size: 13px; color: #666; line-height: 1.4;">${notification.message}</p>
                    <small style="color: #999; font-size: 11px;">${time}</small>
                </div>
                <button onclick="this.parentElement.parentElement.remove()" style="background: none; border: none; color: #999; cursor: pointer; font-size: 16px; padding: 0; margin-left: 10px;">×</button>
            </div>
        `;

        return element;
    }

    getColorForType(color) {
        const colors = {
            'success': '#28a745',
            'warning': '#ffc107',
            'danger': '#dc3545',
            'info': '#17a2b8',
            'primary': '#007bff'
        };
        return colors[color] || '#17a2b8';
    }

    getIconForType(icon) {
        const icons = {
            'shopping-cart': 'shopping-cart',
            'info-circle': 'info-circle',
            'check-circle': 'check-circle',
            'exclamation-triangle': 'exclamation-triangle',
            'times-circle': 'times-circle',
            'cash-register': 'cash-register',
            'sync': 'sync',
            'spinner': 'spinner'
        };
        return icons[icon] || 'bell';
    }

    hideNotification(element) {
        element.style.transform = 'translateX(100%)';
        element.style.opacity = '0';
        setTimeout(() => {
            if (element.parentElement) {
                element.parentElement.removeChild(element);
            }
        }, 300);
    }

    playNotificationSound() {
        try {
            this.notificationSound.currentTime = 0;
            this.notificationSound.play();
        } catch (e) {
            console.log('Не удалось воспроизвести звук уведомления');
        }
    }

    showBrowserNotification(notification) {
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification(notification.title, {
                body: notification.message,
                icon: '/assets/img/favicon.jpg',
                tag: notification.orderId || 'notification'
            });
        }
    }

    updateNotificationCounter() {
        // Обновляем счетчик уведомлений в навигации
        const counter = document.getElementById('notification-counter');
        if (counter) {
            const unreadCount = this.notifications.filter(n => !n.read).length;
            counter.textContent = unreadCount;
            counter.style.display = unreadCount > 0 ? 'block' : 'none';
        }
    }

    handleOrderUpdate(update) {
        // Обновляем данные на страницах заказов
        if (typeof loadOrders === 'function') {
            loadOrders();
        }
        if (typeof loadDashboardStats === 'function') {
            loadDashboardStats();
        }
        if (typeof loadRecentOrders === 'function') {
            loadRecentOrders();
        }
    }

    requestNotificationPermission() {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Глобальный экземпляр менеджера уведомлений
let notificationManager;

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Загружаем WebSocket библиотеки
    if (typeof SockJS === 'undefined') {
        const sockjsScript = document.createElement('script');
        sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
        document.head.appendChild(sockjsScript);
    }
    
    if (typeof Stomp === 'undefined') {
        const stompScript = document.createElement('script');
        stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';
        document.head.appendChild(stompScript);
    }

    // Инициализируем менеджер уведомлений после загрузки библиотек
    setTimeout(() => {
        notificationManager = new NotificationManager();
        notificationManager.requestNotificationPermission();
    }, 1000);
});

// Функция для тестирования уведомлений
function testNotification() {
    if (notificationManager) {
        notificationManager.showNotification({
            type: 'test',
            title: 'Тестовое уведомление',
            message: 'Это тестовое уведомление для проверки системы',
            color: 'info',
            icon: 'info-circle',
            timestamp: new Date().toLocaleTimeString()
        });
    }
} 