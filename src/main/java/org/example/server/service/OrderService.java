package org.example.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.server.DTO.Admin.Order.OrderAdminDTO;
import org.example.server.DTO.Admin.Order.OrderAdminDishDTO;
import org.example.server.DTO.Admin.Order.OrderAdminModifierDTO;
import org.example.server.models.orders.*;
import org.example.server.models.products.Dish;
import org.example.server.models.products.Modifier;
import org.example.server.models.products.Product;
import org.example.server.repositories.DishRepository;
import org.example.server.repositories.ModifierRepository;
import org.example.server.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.example.server.repositories.orders.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final OrderInfoRepository OrderInfoRepository;
    private final OrderResponseRepository OrderResponseRepository;
    private final ModifierRepository modifierRepository;
    private final DishRepository dishRepository;
    private final ProductRepository productRepository;


    private ObjectMapper objectMapper = new ObjectMapper();

    public OrderService(OrderAdressRepositry orderAdressRepositry,
                        OrderCustomerRepository orderCustomerRepository,
                        OrderItemRepository orderItemRepository,
                        OrderModifierRepository orderModifierRepository,
                        OrderPaymentRepository orderPaymentRepository,
                        OrderRepository orderRepository,
                        OrderResponseRepository orderResponseRepository,
                        OrderInfoRepository orderInfoRepository,
                        ModifierRepository modifierRepository,
                        DishRepository dishRepository,
                        ProductRepository productRepository) {
        this.OrderAdressRepositry = orderAdressRepositry;
        this.OrderCustomerRepository = orderCustomerRepository;
        this.OrderItemRepository = orderItemRepository;
        this.OrderRepository = orderRepository;
        this.OrderModifierRepository = orderModifierRepository;
        this.OrderPaymentRepository = orderPaymentRepository;
        this.OrderResponseRepository = orderResponseRepository;
        this.OrderInfoRepository = orderInfoRepository;
        this.modifierRepository = modifierRepository;
        this.dishRepository = dishRepository;
        this.productRepository = productRepository;
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

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Order order = new Order();
        order.setPhone(orderJson.has("phone") ? orderJson.get("phone").asText() : null);
        order.setOrderTypeId(orderJson.has("orderTypeId") ? orderJson.get("orderTypeId").asText() : null);
        order.setComment(orderJson.has("comment") ? orderJson.get("comment").asText() : null);
        order.setCreatedAt(now.format(formatter));
        order.setIpAddress(ipAddress);
        order.setStatus("Pending");
        order.setResponse(null);
        // Сохраняем Address (или null)
        JsonNode deliveryPointJson = orderJson.get("deliveryPoint");
        if (deliveryPointJson != null && deliveryPointJson.has("address")) {
            JsonNode addressJson = deliveryPointJson.get("address");
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
                Dish dish = null;
                Product product = null;
                if(itemJson.has("productId")){
                    String productId = itemJson.get("productId").asText();
                    item.setProductId(productId);
                    dish = dishRepository.findDishById(productId);
                    if (dish == null) {
                        product = productRepository.findProductById(productId);
                        item.setName(product.getName());
                    }   else item.setName(dish.getName());
                }

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
                        OrderModifier orderModifier = new OrderModifier();
                        Modifier modifier = null;

                        if (modifierJson.has("productId")) {
                            String productId = modifierJson.get("productId").asText();
                            orderModifier.setProductId(productId);
                            modifier = modifierRepository.findModifierById(productId);
                            if(modifier == null){
                                dish = dishRepository.findDishById(productId);
                                orderModifier.setNameModifier(dish.getName());
                            }
                            else orderModifier.setNameModifier(modifier.getName());
                        }

                        orderModifier.setAmount(modifierJson.has("amount") ? modifierJson.get("amount").asInt() : 0);
                        orderModifier.setProductGroupId(modifierJson.has("productGroupId") ? modifierJson.get("productGroupId").asText() : null);
                        orderModifier.setItem(item); // Связываем OrderModifier с Item

                        // Сохраняем OrderModifier
                        OrderModifierRepository.save(orderModifier);
                        modifiers.add(orderModifier);
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
    public List<OrderAdminDTO> getAllorder() {
        List<Order> orders = OrderRepository.findAll();
        List<OrderAdminDTO> orderAdminDTOList = new ArrayList<>();
        for (Order order : orders) {
            OrderAdminDTO orderAdminDTO = new OrderAdminDTO();
            orderAdminDTO.setId(order.getId());
            orderAdminDTO.setData(order.getCreatedAt().toString());
            orderAdminDTO.setStatus(order.getStatus());
            orderAdminDTO.setPhone(order.getPhone());
            if (!order.getPayments().isEmpty()) {
                Payment firstPayment = order.getPayments().get(0);
                orderAdminDTO.setPayment(firstPayment.isProcessedExternally());
                orderAdminDTO.setTotal(firstPayment.getSum());
            }
            orderAdminDTO.setDishes(null);
            orderAdminDTOList.add(orderAdminDTO);
        }
        return orderAdminDTOList;
    }

    @Transactional
    public Order getOrder(String id) {
        Order order = OrderRepository.findOrdersById(id);
        OrderRepository.save(order);
        return order;
    }

    @Transactional
    public OrderAdminDTO getOrderByID(String id) {
        Order order = OrderRepository.findOrdersById(id);
        OrderAdminDTO orderAdminDTO = new OrderAdminDTO();
        orderAdminDTO.setId(order.getId());
        orderAdminDTO.setData(order.getCreatedAt().toString());
        orderAdminDTO.setStatus(order.getStatus());
        orderAdminDTO.setPhone(order.getPhone());

        List<OrderAdminDishDTO> dishDTOList = new ArrayList<>();

        if (!order.getPayments().isEmpty()) {
            Payment firstPayment = order.getPayments().get(0);
            orderAdminDTO.setPayment(firstPayment.isProcessedExternally());
            orderAdminDTO.setTotal(firstPayment.getSum());
        }

        for (Item item : order.getItems()) {
            OrderAdminDishDTO dishDTO = new OrderAdminDishDTO();
            dishDTO.setAmount(item.getAmount());
            dishDTO.setNameDish(item.getName());

            List<OrderAdminModifierDTO> modifierDTOList = new ArrayList<>();
            for (OrderModifier modifier : item.getModifiers()) {
                OrderAdminModifierDTO modifierDTO = new OrderAdminModifierDTO();
                modifierDTO.setAmount(modifier.getAmount());
                modifierDTO.setNameModifier(modifier.getNameModifier());
                modifierDTOList.add(modifierDTO);
            }
            dishDTO.setModifiers(modifierDTOList);
            dishDTOList.add(dishDTO);
        }
        orderAdminDTO.setDishes(dishDTOList);
        return orderAdminDTO;
    }

    public void saveResponse(JsonNode responseJson, Order order) {
        log.info("Начало обработки ответа для сохранения. Order ID: {}", order.getId());
        log.info("Начало обработки ответа для сохранения. Json: {}", responseJson.toString());

        try {
            OrderInfo orderInfo = new OrderInfo();
            JsonNode orderInfoJson = responseJson.get("orderInfo");
            if (orderInfoJson != null) {
                log.info("Обнаружен блок 'orderInfo' в ответе.");
                orderInfo.setIdResponse(orderInfoJson.get("id").asText());
                orderInfo.setPosId(orderInfoJson.get("posId").asText());
                orderInfo.setExternalNumber(orderInfoJson.get("externalNumber").asText());
                orderInfo.setOrganizationId(orderInfoJson.get("organizationId").asText());
                orderInfo.setTimestamp(orderInfoJson.get("timestamp").asLong());
                orderInfo.setCreationStatus(orderInfoJson.get("creationStatus").asText());
                orderInfo.setErrorInfo(orderInfoJson.get("errorInfo").asText());
                orderInfo.setOrderData(orderInfoJson.get("order").asText());
                log.info("завершение блока 'orderInfo' в ответе.");
            } else {
                log.warn("Блок 'orderInfo' отсутствует в ответе. Order ID: {}", order.getId());
            }

            Response response = new Response();

            response.setCorrelationId(responseJson.get("correlationId").asText());

            orderInfo.setResponse(response); // Связываем Response с OrderInfo
            response.setOrderInfo(orderInfo);
            order.setResponse(response);

            OrderRepository.save(order);


        } catch (Exception e) {
            log.error("Не удалось сохранить ответ для Order ID: {}. Причина: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Не удалось сохранить ответ", e);
        }

        log.info("Обработка ответа завершена успешно. Order ID: {}", order.getId());
    }


    public void editStatusOrder(Order order, String status) {
        order.setStatus(status);
        OrderRepository.save(order);
    }
}
