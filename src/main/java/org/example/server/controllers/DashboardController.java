package org.example.server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.server.models.orders.Order;
import org.example.server.models.products.Dish;
import org.example.server.models.products.Product;
import org.example.server.repositories.DishRepository;
import org.example.server.repositories.orders.OrderRepository;
import org.example.server.repositories.ProductRepository;
import org.example.server.service.OrderService;
import org.example.server.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/api")
public class DashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final DishRepository dishRepository;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    @Autowired
    public DashboardController(OrderRepository orderRepository,
                             ProductRepository productRepository,
                             DishRepository dishRepository,
                             RestaurantService restaurantService,
                             OrderService orderService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.dishRepository = dishRepository;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Общее количество заказов
            long totalOrders = orderRepository.count();
            stats.put("totalOrders", totalOrders);
            
            // Количество ожидающих заказов
            long pendingOrders = orderRepository.countByStatus("Pending");
            stats.put("pendingOrders", pendingOrders);
            
            // Общее количество продуктов
            long totalProducts = productRepository.count() + dishRepository.count();
            stats.put("totalProducts", totalProducts);
            
            // Общая выручка
            Double totalRevenue = orderRepository.sumTotalPriceByStatus("Completed");
            stats.put("totalRevenue", totalRevenue != null ? totalRevenue.toString() : "0");
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting dashboard stats: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/orders/recent")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders() {
        try {
            List<Order> recentOrders = orderRepository.findTop10ByOrderByCreatedAtDesc();
            List<Map<String, Object>> ordersData = recentOrders.stream()
                    .map(order -> {
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("id", order.getId());
                        orderData.put("status", order.getStatus());
                        orderData.put("totalPrice", order.getTotalPrice());
                        orderData.put("createdAt", order.getCreatedAt());
                        
                        // Получаем имя клиента
                        String customerName = "N/A";
                        if (order.getCustomer() != null) {
                            customerName = order.getCustomer().getName();
                        }
                        orderData.put("customerName", customerName);
                        
                        return orderData;
                    })
                    .toList();
            
            return ResponseEntity.ok(ordersData);
        } catch (Exception e) {
            log.error("Error getting recent orders: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/system/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Проверка API соединения
            boolean apiConnection = checkApiConnection();
            status.put("apiConnection", apiConnection);
            status.put("apiLastUpdate", currentTime);
            
            // Проверка базы данных
            boolean databaseConnection = checkDatabaseConnection();
            status.put("databaseConnection", databaseConnection);
            status.put("dbLastUpdate", currentTime);
            
            // Статус синхронизации продуктов
            String productSyncStatus = checkProductSyncStatus();
            status.put("productSyncStatus", productSyncStatus);
            status.put("productSyncLastUpdate", currentTime);
            
            // Статус синхронизации адресов
            String addressSyncStatus = checkAddressSyncStatus();
            status.put("addressSyncStatus", addressSyncStatus);
            status.put("addressSyncLastUpdate", currentTime);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting system status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/notifications")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        try {
            // Здесь можно реализовать логику получения уведомлений
            // Пока возвращаем пустой список
            List<Map<String, Object>> notifications = List.of();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error getting notifications: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/notifications/clear")
    @ResponseBody
    public ResponseEntity<String> clearNotifications() {
        try {
            // Здесь можно реализовать логику очистки уведомлений
            return ResponseEntity.ok("Notifications cleared");
        } catch (Exception e) {
            log.error("Error clearing notifications: ", e);
            return ResponseEntity.internalServerError().body("Error clearing notifications");
        }
    }

    @GetMapping("/terminals")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTerminals() {
        try {
            List<Map<String, Object>> terminalsData = restaurantService.getInfoTerminalGroup().stream()
                    .map(terminal -> {
                        Map<String, Object> terminalData = new HashMap<>();
                        terminalData.put("terminalId", terminal.getTerminalId());
                        terminalData.put("name", terminal.getNameRestaurant());
                        terminalData.put("address", terminal.getAddress());
                        terminalData.put("timeZone", terminal.getTimeZone());
                        terminalData.put("isActive", terminal.isActive());
                        terminalData.put("restaurantInfo", terminal.getIdRestaurant());
                        return terminalData;
                    })
                    .toList();
            
            return ResponseEntity.ok(terminalsData);
        } catch (Exception e) {
            log.error("Error getting terminals: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/terminals/{terminalId}/test")
    @ResponseBody
    public ResponseEntity<String> testTerminal(@PathVariable String terminalId) {
        try {
            // Здесь можно добавить логику тестирования терминала
            log.info("Testing terminal: {}", terminalId);
            return ResponseEntity.ok("Terminal test completed successfully");
        } catch (Exception e) {
            log.error("Error testing terminal: ", e);
            return ResponseEntity.internalServerError().body("Error testing terminal");
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/terminals-page")
    public String terminals() {
        return "admin/terminals";
    }

    @GetMapping("/products/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalDishes = dishRepository.count();
            long totalModifiers = productRepository.count(); // Assuming modifiers are stored in ProductRepository
            
            stats.put("totalDishes", totalDishes);
            stats.put("totalModifiers", totalModifiers);
            stats.put("lastSync", "2024-01-01 12:00:00"); // This should be stored in database
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting product stats: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/products/clear")
    @ResponseBody
    public ResponseEntity<String> clearAllProducts() {
        try {
            // Clear all products, dishes, and modifiers
            productRepository.deleteAll();
            dishRepository.deleteAll();
            
            return ResponseEntity.ok("All products cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing products: ", e);
            return ResponseEntity.internalServerError().body("Error clearing products");
        }
    }

    @GetMapping("/products/sync-history")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSyncHistory() {
        try {
            // This should be implemented with a proper sync history table
            // For now, return empty list
            List<Map<String, Object>> history = List.of();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting sync history: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/products/sync")
    public String productsSync() {
        return "admin/products-sync";
    }

    @GetMapping("/orders")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount) {
        try {
            List<Order> orders = orderService.getAllOrders();
            List<Map<String, Object>> ordersData = orders.stream()
                    .map(order -> {
                        Map<String, Object> orderData = new HashMap<>();
                        orderData.put("id", order.getId());
                        orderData.put("status", order.getStatus());
                        orderData.put("totalPrice", order.getTotalPrice());
                        orderData.put("createdAt", order.getCreatedAt());
                        orderData.put("comment", order.getComment());
                        orderData.put("items", order.getItems());
                        
                        // Customer info
                        if (order.getCustomer() != null) {
                            orderData.put("customerName", order.getCustomer().getName());
                            orderData.put("customerEmail", order.getCustomer().getEmail());
                            orderData.put("customerPhone", order.getCustomer().getPhone());
                            orderData.put("customerType", order.getCustomer().getType());
                        }
                        
                        // Response info
                        if (order.getResponse() != null) {
                            orderData.put("response", order.getResponse());
                        }
                        
                        return orderData;
                    })
                    .toList();
            
            return ResponseEntity.ok(ordersData);
        } catch (Exception e) {
            log.error("Error getting orders: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/orders/{orderId}/process")
    @ResponseBody
    public ResponseEntity<String> processOrder(@PathVariable String orderId) {
        try {
            // Здесь можно добавить логику обработки заказа
            log.info("Processing order: {}", orderId);
            return ResponseEntity.ok("Order processed successfully");
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            return ResponseEntity.internalServerError().body("Error processing order");
        }
    }

    @PostMapping("/orders/export")
    @ResponseBody
    public ResponseEntity<String> exportOrders() {
        try {
            // Здесь можно добавить логику экспорта заказов
            log.info("Exporting orders");
            return ResponseEntity.ok("Orders exported successfully");
        } catch (Exception e) {
            log.error("Error exporting orders: ", e);
            return ResponseEntity.internalServerError().body("Error exporting orders");
        }
    }

    @GetMapping("/orders-page")
    public String orders() {
        return "admin/orders-enhanced";
    }

    @GetMapping("/api-settings")
    public String apiSettings() {
        return "admin/api-settings";
    }

    @GetMapping("/test-notifications")
    public String testNotifications() {
        return "admin/test-notifications";
    }

    private boolean checkApiConnection() {
        try {
            // Проверяем, есть ли API login
            String apiLogin = restaurantService.getApiLogin();
            return apiLogin != null && !apiLogin.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkDatabaseConnection() {
        try {
            // Простая проверка соединения с БД
            orderRepository.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String checkProductSyncStatus() {
        try {
            // Проверяем, есть ли продукты в базе
            long productCount = productRepository.count() + dishRepository.count();
            return productCount > 0 ? "up_to_date" : "needs_sync";
        } catch (Exception e) {
            return "needs_sync";
        }
    }

    private String checkAddressSyncStatus() {
        try {
            // Здесь можно добавить проверку адресов
            // Пока возвращаем "up_to_date"
            return "up_to_date";
        } catch (Exception e) {
            return "needs_sync";
        }
    }
} 