package org.example.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.server.DTO.Admin.Order.OrderDTO;
import org.example.server.models.orders.*;
import org.springframework.stereotype.Service;
import org.example.server.repositories.orders.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderAdressRepositry OrderAdressRepositry;
    private final OrderCustomerRepository OrderCustomerRepository;
    private final OrderItemRepository OrderItemRepository;
    private final OrderModifierRepository OrderModifierRepository;
    private final OrderPaymentRepository OrderPaymentRepository;
    private final OrderRepository OrderRepository;


    private ObjectMapper objectMapper = new ObjectMapper();

    public OrderService(OrderAdressRepositry orderAdressRepositry,
                        OrderCustomerRepository orderCustomerRepository,
                        OrderItemRepository orderItemRepository,
                        OrderRepository orderRepository,
                        OrderModifierRepository orderModifierRepository,
                        OrderPaymentRepository orderPaymentRepository) {
        this.OrderAdressRepositry = orderAdressRepositry;
        this.OrderCustomerRepository = orderCustomerRepository;
        this.OrderItemRepository = orderItemRepository;
        this.OrderRepository = orderRepository;
        this.OrderModifierRepository = orderModifierRepository;
        this.OrderPaymentRepository = orderPaymentRepository;
    }

    @Transactional
    public void processOrder(JsonNode orderJson, String ipAddress) {
//        List<Order> orders = OrderRepository.findOrdersByIpAddress(ipAddress);
//
//        if (!orders.isEmpty()) {
//            Order lastOrder = orders.get(0);
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime lastOrderTime = lastOrder.getCreatedAt();
//
//            long minutesSinceLastOrder = ChronoUnit.MINUTES.between(lastOrderTime, now);
//
//            if (minutesSinceLastOrder < 30) {
//                log.error("You can only place one order every 30 minutes.");
//                throw new RuntimeException("Order limit exceeded");
//            }
//        }

        Order order = new Order();
        order.setPhone(orderJson.has("phone") ? orderJson.get("phone").asText() : null);
        order.setOrderTypeId(orderJson.has("orderTypeId") ? orderJson.get("orderTypeId").asText() : null);
        order.setComment(orderJson.has("comment") ? orderJson.get("comment").asText() : null);
        order.setCreatedAt(LocalDateTime.now());
        order.setIpAddress(ipAddress);
        order.setStatus(false);
        // Сохраняем Address (или null)
        JsonNode addressJson = orderJson.get("deliveryPoint").get("address");
        if (addressJson != null) {
            Adress address = new Adress();
            address.setStreet(addressJson.has("street") ? addressJson.get("street").get("id").asText() : null);
            address.setHouse(addressJson.has("house") ? addressJson.get("house").asText() : null);
            address.setType(addressJson.has("type") ? addressJson.get("type").asText() : null);
            address.setEntrance(addressJson.has("entrance") ? addressJson.get("entrance").asText() : null);
            address.setFlat(addressJson.has("flat") ? addressJson.get("flat").asText() : null);
            address.setFloor(addressJson.has("floor") ? addressJson.get("floor").asText() : null);
            address.setIntercom(addressJson.has("doorphone") ? addressJson.get("doorphone").asText() : null);
            address = OrderAdressRepositry.save(address); // Сохраняем адрес
            order.setAddress(address);
        } else {
            order.setAddress(null); // Разрешаем отсутствие адреса
        }

        // Сохраняем Customer (или null)
        JsonNode customerJson = orderJson.get("customer");
        if (customerJson != null) {
            Customer customer = new Customer();
            customer.setName(customerJson.has("name") ? customerJson.get("name").asText() : null);
            customer.setType(customerJson.has("type") ? customerJson.get("type").asText() : null);
            customer = OrderCustomerRepository.save(customer); // Сохраняем клиента
            order.setCustomer(customer);
        } else {
            order.setCustomer(null);
        }

        // Сохраняем Order перед Items
        order = OrderRepository.save(order);

        // Сохраняем Items
        List<Item> items = new ArrayList<>();
        JsonNode itemsJson = orderJson.get("items");

        if (itemsJson != null) {
            for (JsonNode itemJson : itemsJson) {
                Item item = new Item();
                item.setProductId(itemJson.has("productId") ? itemJson.get("productId").asText() : null);
                item.setAmount(itemJson.has("amount") ? itemJson.get("amount").asInt() : 0);
                item.setOrder(order);  // Связываем Item с Order

                // Сохраняем Item
                item = OrderItemRepository.save(item);
                items.add(item); // Добавляем в список items

                // Сохраняем модификаторы
                JsonNode modifiersJson = itemJson.get("modifiers");
                if (modifiersJson != null) {
                    List<OrderModifier> modifiers = new ArrayList<>();
                    for (JsonNode modifierJson : modifiersJson) {
                        OrderModifier modifier = new OrderModifier();
                        modifier.setProductId(modifierJson.has("productId") ? modifierJson.get("productId").asText() : null);
                        modifier.setAmount(modifierJson.has("amount") ? modifierJson.get("amount").asInt() : 0);
                        modifier.setProductGroupId(modifierJson.has("productGroupId") ? modifierJson.get("productGroupId").asText() : null);
                        modifier.setItem(item); // Связываем OrderModifier с Item

                        // Сохраняем OrderModifier
                        modifier = OrderModifierRepository.save(modifier);
                        modifiers.add(modifier);
                    }
                    item.setModifiers(modifiers); // Устанавливаем модификаторы в item
                }
            }
            order.setItems(items); // Устанавливаем items в order
        }

        // Сохраняем Payments
        JsonNode paymentsJson = orderJson.get("payments");
        if (paymentsJson != null) {
            List<Payment> payments = new ArrayList<>();
            for (JsonNode paymentJson : paymentsJson) {
                Payment payment = new Payment();
                payment.setPaymentTypeKind(paymentJson.has("paymentTypeKind") ? paymentJson.get("paymentTypeKind").asText() : null);
                payment.setSum(paymentJson.has("sum") ? paymentJson.get("sum").asDouble() : 0.0);
                payment.setProcessedExternally(paymentJson.has("isProcessedExternally") && paymentJson.get("isProcessedExternally").asBoolean());
                payment.setOrder(order);
                payment = OrderPaymentRepository.save(payment);
                payments.add(payment);
            }
            order.setPayments(payments);
        }

        try {
            String jsonString = objectMapper.writeValueAsString(orderJson); // Сериализация JSON в строку
            order.setJson(jsonString); // Сохраняем сериализованный JSON в объект заказа
            // Сохранение в базу данных
            OrderRepository.save(order);
        } catch (Exception e) {
            // Обработка исключений
            e.printStackTrace();
        }

    }

    @Transactional
    public List<OrderDTO> getAllorder() {
        List<Order> orders = OrderRepository.findAll();
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Order order : orders) {
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setId(order.getId());
            orderDTO.setData(order.getCreatedAt().toString());
            orderDTO.setStatus(order.isStatus());
            orderDTO.setPhone(order.getPhone());
            if (!order.getPayments().isEmpty()) {
                Payment firstPayment = order.getPayments().get(0);
                orderDTO.setPayment(firstPayment.isProcessedExternally());
                orderDTO.setTotal(firstPayment.getSum());
            }
            orderDTOList.add(orderDTO);
        }
        return orderDTOList;
    }

    @Transactional
    public Order getOrder(String id) {
        Order order = OrderRepository.findOrdersById(id);
        order.setStatus(true);
        OrderRepository.save(order);
        return order;
    }
}
