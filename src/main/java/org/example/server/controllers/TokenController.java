package org.example.server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.server.models.OrderRequest;
import org.example.server.models.orders.Order;
import org.example.server.models.products.Dish;
import org.example.server.models.products.DishModifier;
import org.example.server.models.products.DishWithModifiers;
import org.example.server.models.products.Product;
import org.example.server.repositories.DishModifierRepository;
import org.example.server.repositories.DishRepository;
import org.example.server.service.AdressService;
import org.example.server.service.OrderService;
import org.example.server.service.ProductService;
import org.example.server.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Controller
public class TokenController {

    private final OrderController orderController;
    ObjectMapper mapper = new ObjectMapper();

    private int requestAttempts = 0;

    String terminalTestGroupId = "753a78c4-8802-49c3-8d3b-64dd7b001a10";

    private final ProductService productService;
    private final DishModifierRepository dishModifierRepository;
    private final RestaurantService restaurantService;
    private final AdressService adressService;
    private final OrderService orderService;

    public TokenController(ProductService productService, DishModifierRepository dishModifierRepository, RestaurantService restaurantService, AdressService adressService, OrderService orderService, OrderController orderController) {
        this.productService = productService;
        this.dishModifierRepository = dishModifierRepository;
        this.restaurantService = restaurantService;
        this.adressService = adressService;
        this.orderService = orderService;
        this.orderController = orderController;
    }

    private static final String API_IIKO = "https://api-ru.iiko.services/api/";
    private static final String TOKEN_URL = API_IIKO + "1/access_token";
    private static final String ORGANIZATIONS_URL = API_IIKO + "1/organizations";
    private static final String NOMENCLATURE_URL = API_IIKO + "1/nomenclature";
    private static final String CITIES_URL = API_IIKO + "1/cities";
    private static final String STREET_URL = API_IIKO + "1/streets/by_city";
    private static final String TERMINAL_URL = API_IIKO + "1/terminal_groups";
    private static final String ORDER_URL = API_IIKO + "1/deliveries/create";
    private static final String STATUS_ORDER_URL = API_IIKO + "1/commands/status";
    private static final String STATUS_TERMINAL_URL = API_IIKO + "1/terminal_groups/is_alive";

    private String token;
    private boolean apiLoginNotFound;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();

    @GetMapping("/admin/getToken")
    public ResponseEntity<String> getToken() {
        String apiLogin;
        try {
            apiLogin = restaurantService.getApiLogin();
        } catch (NoSuchElementException e) {
            apiLoginNotFound = true;
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка: API login не введен.");
        }

        String requestBody = "{\"apiLogin\": \"" + apiLogin + "\"}";
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        log.warn("Получение токена");
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            apiLoginNotFound = false;
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            token = jsonNode.get("token").asText();
            return ResponseEntity.ok("Токен успешно получен");

        }

        catch (HttpClientErrorException e) {
            apiLoginNotFound = true;
            log.error("Ошибка при получении токена: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении токена: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка обработки JSON", e);
        }
    }


    @GetMapping("/getOrganization")
    public ResponseEntity<String> getOrganization() {
        if (apiLoginNotFound) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка: API login не введен.");
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        String requestBody = "{}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    ORGANIZATIONS_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
            JsonNode organizationsNode = jsonNode.get("organizations").get(0);
            String idRestaurant = organizationsNode.get("id").asText();
            String nameRestaurant = organizationsNode.get("name").asText();
            String apiLogin = restaurantService.getApiLogin();
            restaurantService.saveOrUpdateRestaurant(idRestaurant, nameRestaurant, apiLogin);
            return ResponseEntity.ok("Информация об организации успешно получена и сохранена.");

        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                getToken();
                return getOrganization();
            } else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при получении организации: " + e.getMessage());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка обработки JSON", e);
        }
    }

    @GetMapping("/admin/saveProducts")
    public String saveProducts(Model model) {
        if (apiLoginNotFound) {
            log.warn("API login not found or incorrect.");
            model.addAttribute("Error", "Не введен логин или он не верный");
            apiLoginNotFound = false;
            return "test/tokenResult";
        }

        String idRestaurant;
        try {
            idRestaurant = restaurantService.getIdRestaurant();
        } catch (NoSuchElementException e) {
            getOrganization(); // Initialize idRestaurant if not found
            return "redirect:/admin/saveProducts";
        }

        if (idRestaurant == null || idRestaurant.isEmpty()) {
            getOrganization(); // Initialize idRestaurant if not found
            return "redirect:/admin/saveProducts";
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        String requestBody = "{\"organizationId\": \"" + idRestaurant + "\"}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    NOMENCLATURE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            productService.saveProductsFromJson(responseEntity.getBody());
            log.info("Products saved successfully.");
            model.addAttribute("body", responseEntity.getBody());

        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                getToken();
                return "redirect:/admin/saveProducts";
            }
            if (e.getRawStatusCode() == 400){
                getOrganization();
                return "redirect:/admin/saveProducts";
            }
            else {
                model.addAttribute("Error", "Ошибка при сохранении продуктов: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка обработки JSON", e);
        }

        return "test/tokenResult";
    }


    @PostMapping("/admin/resetToken")
    public ResponseEntity<String> resetToken() {
        token = null; // Обнуляем токен
        return ResponseEntity.ok("Токен успешно сброшен.");
    }

    @GetMapping("/api/menu")
    @ResponseBody
    public Map<String, Object> getMenuApi() {
        List<Product> products = productService.listProduct();
        List<Dish> dishes = productService.listDish();
        List<DishWithModifiers> dishesWithModifiers = new ArrayList<>();

        for (Dish dish : dishes) {
            List<DishModifier> dishModifiers = dishModifierRepository.findByDishId(dish.getId());
            DishWithModifiers dishWithModifiers = new DishWithModifiers(dish, dishModifiers);
            dishesWithModifiers.add(dishWithModifiers);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("dishes", dishesWithModifiers);

        return result;
    }

    @RestController
    @RequestMapping("/api/dishes")
    public class DishController {

        @Autowired
        private DishRepository dishRepository;

        @GetMapping
        public List<Dish> getAllDishes() {
            return dishRepository.findAll();
        }
    }

    @GetMapping("/admin/getTerminalGroup")
    public String TerminalGroup(Model model) {
        if (apiLoginNotFound) {
            log.warn("API login not found or incorrect.");
            model.addAttribute("Error", "Не введен логин или он не верный");
            apiLoginNotFound = false;
            return "test/tokenResult";
        }

        String idRestaurant;
        try {
            idRestaurant = restaurantService.getIdRestaurant();
        } catch (NoSuchElementException e) {
            getOrganization(); // Initialize idRestaurant if not found
            requestAttempts++;
            return "redirect:/admin/getTerminalGroup";
        }

        if (idRestaurant == null || idRestaurant.isEmpty()) {
            getOrganization(); // Initialize idRestaurant if not found
            requestAttempts++;
            return "redirect:/admin/getTerminalGroup";
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        String requestBody = "{\"organizationIds\": [\"" + idRestaurant + "\"]," + "\"includeDisabled\": true" + "}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    TERMINAL_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            model.addAttribute("body", responseEntity.getBody());
            requestAttempts = 0;
            JsonNode terminalGroup = mapper.readTree(responseEntity.getBody());
            restaurantService.saveTerminalGroupFromJson(terminalGroup);

        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401 && requestAttempts<10) {
                getToken();
                requestAttempts++;
                return "redirect:/admin/getTerminalGroup";
            }
            if (e.getRawStatusCode() == 400 && requestAttempts<10) {
                getOrganization();
                requestAttempts++;
                return "redirect:/admin/getTerminalGroup";
            } else {
                model.addAttribute("Error", "Ошибка при сохранении терминала: " + e.getMessage() + "Код ошибки" + e.getRawStatusCode()) ;
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return "test/tokenResult";
    }


    @PostMapping("/ordering")
    public ResponseEntity<String> processOrder(@RequestBody String orderId) {
        log.info("Received order ID: {}", orderId);

        try {
            // Асинхронный вызов для обработки заказа
            CompletableFuture<String> orderProcessing = processOrderAsync(orderId);

            // Ожидание результата асинхронной операции
            String result = orderProcessing.join();  // или orderProcessing.get(), если нужно обработать исключение

            // Возвращаем результат выполнения
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    @Async
    public CompletableFuture<String> processOrderAsync(String orderId) {
        try {
            // Получение заказа
            Order order = orderService.getOrder(orderId);

            if (apiLoginNotFound) {
                log.warn("API login not found or incorrect.");
                apiLoginNotFound = false;
                throw new IllegalStateException("API login not found or incorrect.");
            }

            // Получение ID ресторана
            String restaurantId = fetchRestaurantId();
            if (restaurantId == null) {
                log.warn("Failed to fetch or initialize Restaurant ID.");
                throw new IllegalArgumentException("Unable to fetch restaurant ID.");
            }

            // Создание и отправка заказа
            OrderRequest orderRequest = createOrderRequest(order.getJson(), restaurantId);
            log.info("Отправка заказ: " + order.getJson());
            JsonNode orderResponse = processOrderWithRetries(orderRequest);
            if (orderResponse == null) {
                log.error("Failed to process order after retries.");
                orderService.editStatusOrder(order, "ERROR");
                throw new RuntimeException("Failed to process order.");
            }

            // Сохранение ответа
            orderService.saveResponse(orderResponse, order);
            String correlationId = orderResponse.get("correlationId").asText();

            //Проверка статуса терминала
            if(checkTerminalStatus(restaurantId)) {
                // Проверка статуса заказа
                String orderStatus = checkOrderStatus(correlationId, restaurantId);
                if ("Success".equals(orderStatus)) {
                    orderService.editStatusOrder(order, "Completed");
                    log.info("Order successfully completed.");
                    return CompletableFuture.completedFuture("Order successfully completed.");
                } else if ("Error".equals(orderStatus)) {
                    orderService.editStatusOrder(order, "ERROR");
                    throw new IllegalStateException("Не возможно доставить заказ");
                }
            }
            else {
                orderService.editStatusOrder(order, "ERROR");
                throw new IllegalStateException("Касса не доступна");
            }

        } catch (Exception e) {
            log.error("Error in async order processing: ", e);
            return CompletableFuture.failedFuture(e);
        }
        return CompletableFuture.completedFuture("Order successfully completed.");
    }


    private String fetchRestaurantId() {
        try {
            return restaurantService.getIdRestaurant();
        } catch (NoSuchElementException e) {
            log.warn("Restaurant ID not found. Fetching organization data...");
            getOrganization();
            return restaurantService.getIdRestaurant();
        }
    }

    private OrderRequest createOrderRequest(String json, String restaurantId) throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.organizationId = restaurantId;
        orderRequest.terminalGroupId = terminalTestGroupId;
        orderRequest.createOrderSettings = new OrderRequest.CreateOrderSettings();
        orderRequest.order = new ObjectMapper().readTree(json);
        return orderRequest;
    }

    private JsonNode processOrderWithRetries(OrderRequest orderRequest) {
        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("Processing order, attempt {}", attempt);
                String requestBody = new ObjectMapper().writeValueAsString(orderRequest);
                HttpHeaders headers = createHeaders();
                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        ORDER_URL, HttpMethod.POST, requestEntity, String.class
                );

                return new ObjectMapper().readTree(responseEntity.getBody());
            } catch (HttpClientErrorException e) {
                handleHttpClientError(e);
            } catch (Exception e) {
                log.error("Error during order processing: {}", e.getMessage());
                break;
            }
        }
        return null;
    }

    private boolean checkTerminalStatus(String restaurantId) {
        try {
            HttpHeaders headers = createHeaders();
            String requestBody = String.format(
                    "{\"organizationId\": \"%s\", \"terminalGroupIds\": [\"%s\"]}",
                    restaurantId, terminalTestGroupId
            );

            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    STATUS_TERMINAL_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class
            );

            JsonNode responseBody = new ObjectMapper().readTree(responseEntity.getBody());
            JsonNode isAliveStatusNode = responseBody.get("isAliveStatus");

            for (JsonNode statusNode : isAliveStatusNode) {
                if (statusNode.get("isAlive").asBoolean()) {
                    return true;
                }else {
                    log.warn("Missing 'isAliveStatus' field in response.");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error checking Terminal status: {}", e.getMessage(), e);
        }
        return false;
    }



    private String checkOrderStatus(String correlationId, String restaurantId) {
        int delayBetweenRetries = 10000;
        for (int attempt = 1; attempt <= 15; attempt++) {
            try {
                log.info("Checking order status, attempt {}", attempt);
                HttpHeaders headers = createHeaders();
                String requestBody = String.format(
                        "{\"organizationId\": \"%s\", \"correlationId\": \"%s\"}",
                        restaurantId, correlationId
                );

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        STATUS_ORDER_URL, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class
                );

                JsonNode responseBody = new ObjectMapper().readTree(responseEntity.getBody());
                String state = responseBody.get("state").asText();
                if ("Success".equals(state)) {
                    log.info("Order status successfully completed.");
                    return "Success";
                } else if ("InProgress".equals(state)) {
                } else if ("Error".equals(state)){
                    String error = responseBody.get("exception").get("message").asText();
                    log.error("Error: "+ error);
                    return "Error";
                }
                log.warn("Status: "+ state);
                Thread.sleep(delayBetweenRetries);
            } catch (Exception e) {
                log.error("Error checking order status: {}", e.getMessage());
            }
        }
        return "Failed";
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    private void handleHttpClientError(HttpClientErrorException e) {
        int statusCode = e.getRawStatusCode();
        if (statusCode == 401) {
            log.warn("Unauthorized (401). Refreshing token...");
            getToken();
        } else if (statusCode == 400) {
            log.warn("Bad Request (400). Refreshing organization data...");
            getOrganization();
        } else {
            log.error("HTTP error: {}", e.getMessage());
        }
    }



    @GetMapping("/admin/saveAddress")
    public ResponseEntity<?> saveCities() {
        if (apiLoginNotFound) {
            log.warn("API login not found or incorrect.");
            apiLoginNotFound = false;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("API login not found or incorrect.");
        }

        String idRestaurant;
        try {
            idRestaurant = restaurantService.getIdRestaurant();
        } catch (NoSuchElementException e) {
            getOrganization(); // Initialize idRestaurant if not found
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .header("Location", "/admin/saveAddress")
                    .build();
        }

        if (idRestaurant == null || idRestaurant.isEmpty()) {
            getOrganization(); // Initialize idRestaurant if not found
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .header("Location", "/admin/saveAddress")
                    .build();
        }

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        String requestBody = "{\"organizationIds\": [\"" + idRestaurant + "\"]}";
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    CITIES_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            JsonNode rootNode = mapper.readTree(responseEntity.getBody());
            JsonNode cities = rootNode.path("cities").get(0).path("items");

            adressService.saveCityFromJson(cities);

            for (JsonNode city : cities) {
                String cityID = city.path("id").asText();
                requestBody = "{\"organizationId\": \"" + idRestaurant + "\",\"cityId\":\"" + cityID + "\"}";
                requestEntity = new HttpEntity<>(requestBody, headers);
                responseEntity = restTemplate.exchange(
                        STREET_URL,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );
                rootNode = mapper.readTree(responseEntity.getBody());
                JsonNode street = rootNode.path("streets");
                adressService.saveStreetFromJson(street, cityID);
            }
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 401) {
                getToken();
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header("Location", "/admin/saveAddress")
                        .build();
            }
            if (e.getRawStatusCode() == 400) {
                getOrganization();
                return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .header("Location", "/admin/saveAddress")
                        .build();
            }
            log.error("Error saving cities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving cities: " + e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing response JSON: " + e.getMessage());
        }

        return ResponseEntity.ok("Streets updated successfully.");
    }
}