package org.example.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.server.DTO.Admin.Order.*;
import org.example.server.models.adress.Streets;
import org.example.server.models.orders.*;
import org.example.server.models.products.Dish;
import org.example.server.models.products.Modifier;
import org.example.server.models.products.Product;
import org.example.server.repositories.*;
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
    private final ModifierRepository modifierRepository;
    private final DishRepository dishRepository;
    private final ProductRepository productRepository;
    private final StreetsRepository streetsRepository;


    private ObjectMapper objectMapper = new ObjectMapper();

    public OrderService(OrderAdressRepositry orderAdressRepositry,
                        OrderCustomerRepository orderCustomerRepository,
                        OrderItemRepository orderItemRepository,
                        OrderModifierRepository orderModifierRepository,
                        OrderPaymentRepository orderPaymentRepository,
                        OrderRepository orderRepository,
                        ModifierRepository modifierRepository,
                        DishRepository dishRepository,
                        ProductRepository productRepository,
                        StreetsRepository streetsRepository) {
        this.OrderAdressRepositry = orderAdressRepositry;
        this.OrderCustomerRepository = orderCustomerRepository;
        this.OrderItemRepository = orderItemRepository;
        this.OrderRepository = orderRepository;
        this.OrderModifierRepository = orderModifierRepository;
        this.OrderPaymentRepository = orderPaymentRepository;
        this.modifierRepository = modifierRepository;
        this.dishRepository = dishRepository;
        this.productRepository = productRepository;
        this.streetsRepository = streetsRepository;
    }

    @Transactional
    public void processOrder(JsonNode cart, String ipAddress) {
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

        JsonNode orderJson = cart.get("json");

        String email = cart.get("email").asText();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Order order = new Order();
        order.setOrderTypeId(orderJson.has("orderTypeId") ? orderJson.get("orderTypeId").asText() : null);
        order.setComment(orderJson.has("comment") ? orderJson.get("comment").asText() : null);
        order.setCreatedAt(now.format(formatter));
        order.setIpAddress(ipAddress);
        order.setStatus("Pending");
        order.setTotalPrice(cart.get("totalPrice").asText());
        order.setResponse(null);
        // Сохраняем Address (или null)
        JsonNode deliveryPointJson = orderJson.get("deliveryPoint");
        if (deliveryPointJson != null && deliveryPointJson.has("address")) {
            JsonNode addressJson = deliveryPointJson.get("address");
            Adress address = new Adress();

            // Получение улицы и города из репозитория
            if (addressJson.has("street") && addressJson.get("street").has("id")) {
                String streetId = addressJson.get("street").get("id").asText();
                Streets streets = streetsRepository.findStreetById(streetId);
                if (streets != null) {
                    address.setStreetName(streets.getName());
                    address.setCityName(streets.getCity().getCity());
                    address.setStreetID(streetId);
                }
            }
            // Заполнение остальных полей адреса
            address.setHouse(addressJson.has("house") ? addressJson.get("house").asText() : null);
            address.setType(addressJson.has("type") ? addressJson.get("type").asText() : null);
            address.setEntrance(addressJson.has("entrance") ? addressJson.get("entrance").asText() : null);
            address.setFlat(addressJson.has("flat") ? addressJson.get("flat").asText() : null);
            address.setFloor(addressJson.has("floor") ? addressJson.get("floor").asText() : null);
            address.setIntercom(addressJson.has("doorphone") ? addressJson.get("doorphone").asText() : null);

            // Сохранение адреса
            address = OrderAdressRepositry.save(address);
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
            customer.setPhone(orderJson.has("phone") ? orderJson.get("phone").asText() : null);
            customer.setEmail(email);
            customer = OrderCustomerRepository.save(customer); // Сохраняем клиента
            order.setCustomer(customer);
        } else {
            order.setCustomer(null);
        }

        // Сохраняем Order перед Items
        order = OrderRepository.save(order);

        // Сохраняем Items
        List<Item> items = new ArrayList<>();
        JsonNode itemsJson = cart.get("dishes");

        if (itemsJson != null) {
            for (JsonNode itemJson : itemsJson) {
                Item item = new Item();
                item.setAmount(itemJson.has("amount") ? itemJson.get("amount").asInt() : 0);
                item.setName(itemJson.has("name") ? itemJson.get("name").asText() : null);
                item.setImageLinks(itemJson.has("imageLinks") ? itemJson.get("imageLinks").asText() : null);
                item.setPrice(itemJson.has("price") ? itemJson.get("price").asText() : null);
                item.setDiscount(itemJson.has("discount") ? itemJson.get("discount").asText() : null);
                item.setSubtotal(itemJson.has("subtotal") ? itemJson.get("subtotal").asText() : null);
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

                        orderModifier.setAmount(modifierJson.has("amount") ? modifierJson.get("amount").asInt() : 0);
                        orderModifier.setName(modifierJson.has("name") ? modifierJson.get("name").asText() : null);
                        orderModifier.setPrice(modifierJson.has("price") ? modifierJson.get("price").asText() : null);
                        orderModifier.setSubtotal(modifierJson.has("subtotal") ? modifierJson.get("subtotal").asText() : null);
                        orderModifier.setItem(item); // Связываем OrderModifier с Item

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
            orderAdminDTO.setPhone(order.getCustomer().getPhone());
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
        List<OrderAdminDishDTO> dishDTOList = new ArrayList<>();

        if (!order.getPayments().isEmpty()) {
            Payment firstPayment = order.getPayments().get(0);
            orderAdminDTO.setPayment(firstPayment.isProcessedExternally());
            orderAdminDTO.setTotal(firstPayment.getSum());
        }

        OrderAdminAddressDTO orderAdminAddressDTO = new OrderAdminAddressDTO();
        if (order.getAddress() != null) {
            orderAdminAddressDTO.setHouse(order.getAddress().getHouse());
            orderAdminAddressDTO.setFloor(order.getAddress().getFloor());
            orderAdminAddressDTO.setIntercom(order.getAddress().getIntercom());
            orderAdminAddressDTO.setEntrance(order.getAddress().getEntrance());
            orderAdminAddressDTO.setFlat(order.getAddress().getFlat());

            orderAdminAddressDTO.setCity(order.getAddress().getCityName());
            orderAdminAddressDTO.setStreet(order.getAddress().getStreetName());

        }
        else orderAdminAddressDTO = null;

        OrderAdminCustomerDTO orderAdminCustomerDTO = new OrderAdminCustomerDTO();
        if (order.getCustomer() != null) {
            orderAdminCustomerDTO.setName(order.getCustomer().getName());
            orderAdminCustomerDTO.setPhone(order.getCustomer().getPhone());
            orderAdminCustomerDTO.setEmail(order.getCustomer().getEmail());
        }

        for (Item item : order.getItems()) {
            OrderAdminDishDTO dishDTO = new OrderAdminDishDTO();
            dishDTO.setAmount(item.getAmount());
            dishDTO.setName(item.getName());
            dishDTO.setPrice(item.getPrice());
            dishDTO.setDiscount(item.getDiscount());
            dishDTO.setSubtotal(item.getSubtotal());
            dishDTO.setImageLinks(item.getImageLinks());

            List<OrderAdminModifierDTO> modifierDTOList = new ArrayList<>();
            for (OrderModifier modifier : item.getModifiers()) {
                OrderAdminModifierDTO modifierDTO = new OrderAdminModifierDTO();
                modifierDTO.setAmount(modifier.getAmount());
                modifierDTO.setName(modifier.getName());
                modifierDTO.setPrice(modifier.getPrice());
                modifierDTO.setSubtotal(modifier.getSubtotal());

                modifierDTOList.add(modifierDTO);
            }
            dishDTO.setModifiers(modifierDTOList);
            dishDTOList.add(dishDTO);
        }
        orderAdminDTO.setDishes(dishDTOList);
        orderAdminDTO.setAddress(orderAdminAddressDTO);
        orderAdminDTO.setCustomer(orderAdminCustomerDTO);
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
