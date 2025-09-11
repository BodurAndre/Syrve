package org.example.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String NOTIFICATION_TOPIC = "/topic/notifications";
    private static final String ORDER_TOPIC = "/topic/orders";

    public void sendNewOrderNotification(String orderId, String customerName, String totalPrice) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "new_order");
        notification.put("title", "Новый заказ!");
        notification.put("message", "Получен новый заказ от " + customerName);
        notification.put("orderId", orderId);
        notification.put("totalPrice", totalPrice);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        notification.put("icon", "shopping-cart");
        notification.put("color", "success");

        // Отправляем уведомление всем подключенным клиентам
        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
        
        // Отправляем обновление данных заказов
        sendOrderUpdate();
        
        log.info("Отправлено уведомление о новом заказе: {}", orderId);
    }

    public void sendOrderStatusNotification(String orderId, String status, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "order_status");
        notification.put("title", "Статус заказа изменен");
        notification.put("message", message);
        notification.put("orderId", orderId);
        notification.put("status", status);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        notification.put("icon", "info-circle");
        notification.put("color", "info");

        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
        sendOrderUpdate();
        
        log.info("Отправлено уведомление об изменении статуса заказа: {} -> {}", orderId, status);
    }

    public void sendSystemNotification(String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "system");
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        switch (type.toLowerCase()) {
            case "success":
                notification.put("icon", "check-circle");
                notification.put("color", "success");
                break;
            case "warning":
                notification.put("icon", "exclamation-triangle");
                notification.put("color", "warning");
                break;
            case "error":
                notification.put("icon", "times-circle");
                notification.put("color", "danger");
                break;
            default:
                notification.put("icon", "info-circle");
                notification.put("color", "info");
        }

        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
        log.info("Отправлено системное уведомление: {}", title);
    }

    public void sendOrderUpdate() {
        // Отправляем сигнал для обновления данных заказов
        Map<String, Object> update = new HashMap<>();
        update.put("type", "order_update");
        update.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        messagingTemplate.convertAndSend(ORDER_TOPIC, update);
    }

    public void sendTerminalStatusNotification(String terminalId, String status) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "terminal_status");
        notification.put("title", "Статус терминала изменен");
        notification.put("message", "Терминал " + terminalId + " изменил статус на " + status);
        notification.put("terminalId", terminalId);
        notification.put("status", status);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        notification.put("icon", "cash-register");
        notification.put("color", "info");

        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
        log.info("Отправлено уведомление о статусе терминала: {} -> {}", terminalId, status);
    }

    public void sendSyncNotification(String type, String status, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "sync_" + type);
        notification.put("title", "Синхронизация " + type);
        notification.put("message", message);
        notification.put("syncType", type);
        notification.put("status", status);
        notification.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        if ("success".equals(status)) {
            notification.put("icon", "sync");
            notification.put("color", "success");
        } else if ("error".equals(status)) {
            notification.put("icon", "exclamation-triangle");
            notification.put("color", "danger");
        } else {
            notification.put("icon", "spinner");
            notification.put("color", "info");
        }

        messagingTemplate.convertAndSend(NOTIFICATION_TOPIC, notification);
        log.info("Отправлено уведомление о синхронизации: {} -> {}", type, status);
    }
} 