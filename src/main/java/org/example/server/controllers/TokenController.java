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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;


@Slf4j
@Controller
public class TokenController {

    ObjectMapper mapper = new ObjectMapper();

    private int requestAttempts = 0;

    String terminalTestGroupId = "753a78c4-8802-49c3-8d3b-64dd7b001a10";

    private final ProductService productService;
    private final DishModifierRepository dishModifierRepository;
    private final RestaurantService restaurantService;
    private final AdressService adressService;
    private final OrderService orderService;

    public TokenController(ProductService productService, DishModifierRepository dishModifierRepository, RestaurantService restaurantService, AdressService adressService, OrderService orderService) {
        this.productService = productService;
        this.dishModifierRepository = dishModifierRepository;
        this.restaurantService = restaurantService;
        this.adressService = adressService;
        this.orderService = orderService;
    }

    private static final String API_IIKO = "https://api-ru.iiko.services/api/";
    private static final String TOKEN_URL = API_IIKO + "1/access_token";
    private static final String ORGANIZATIONS_URL = API_IIKO + "1/organizations";
    private static final String NOMENCLATURE_URL = API_IIKO + "1/nomenclature";
    private static final String CITIES_URL = API_IIKO + "1/cities";
    private static final String STREET_URL = API_IIKO + "1/streets/by_city";
    private static final String TERMINAL_URL = API_IIKO + "1/terminal_groups";
    private static final String ORDER_URL = API_IIKO + "1/deliveries/create";

    private String token;
    private boolean apiLoginNotFound;

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    HttpHeaders headers = new HttpHeaders();

    @GetMapping("/getToken")
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

        } catch (HttpClientErrorException e) {
            apiLoginNotFound = true;
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
    public ResponseEntity<?> processOrder(@RequestBody String id) {
        log.info("Order ID: " + id);
        Order order = orderService.getOrder(id);
        try {
            // Проверка API логина
            if (apiLoginNotFound) {
                log.warn("API login not found or incorrect.");
                apiLoginNotFound = false;
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("API login not found or incorrect.");
            }

            // Попытка получить idRestaurant
            String idRestaurant = getOrFetchIdRestaurant();

            if (idRestaurant == null || idRestaurant.isEmpty()) {
                log.warn("Failed to fetch or initialize Restaurant ID.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Unable to fetch restaurant ID.");
            }

            // Преобразование JSON в объект OrderRequest
            OrderRequest newOrderRequest = createOrderRequest(order.getJson(), idRestaurant);


            // Попытка отправить заказ
            String response = tryToProcessOrder(newOrderRequest);
            if (response == null) {
                log.error("Failed to process order after retries.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to process order.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getMessage());
        }
    }

    // Метод для получения ID ресторана
    private String getOrFetchIdRestaurant() {
        try {
            return restaurantService.getIdRestaurant();
        } catch (NoSuchElementException e) {
            log.warn("Restaurant ID not found. Fetching organization data...");
            getOrganization(); // Обновить данные
            return restaurantService.getIdRestaurant();
        }
    }

    // Метод для создания OrderRequest
    private OrderRequest createOrderRequest(String json, String idRestaurant) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        OrderRequest newOrderRequest = new OrderRequest();
        newOrderRequest.organizationId = idRestaurant;
        newOrderRequest.terminalGroupId = terminalTestGroupId;
        newOrderRequest.createOrderSettings = new OrderRequest.CreateOrderSettings();
        JsonNode orderNode = objectMapper.readTree(json);
        newOrderRequest.order = orderNode;
        return newOrderRequest;
    }

    // Метод для обработки заказа с повторными попытками
    private String tryToProcessOrder(OrderRequest newOrderRequest) {
        int maxRetries = 3; // Максимальное количество попыток
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("Attempt {} to process order...", attempt);
                String updatedJson = new ObjectMapper().writeValueAsString(newOrderRequest);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + token);

                HttpEntity<String> requestEntity = new HttpEntity<>(updatedJson, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        ORDER_URL,
                        HttpMethod.POST,
                        requestEntity,
                        String.class
                );

                log.info("Order successfully processed on attempt {}.", attempt);
                return responseEntity.getBody();
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == 401) {
                    log.warn("Unauthorized (401). Refreshing token...");
                    getToken(); // Обновить токен
                } else if (e.getRawStatusCode() == 400) {
                    log.warn("Bad Request (400). Fetching organization data...");
                    getOrganization(); // Обновить данные
                } else {
                    log.error("HTTP error: {}", e.getMessage());
                    break; // Прерываем цикл при других ошибках
                }
            } catch (Exception e) {
                log.error("Error during order processing attempt {}: {}", attempt, e.getMessage());
                break; // Прерываем цикл при других исключениях
            }
        }
        return null; // Если не удалось обработать заказ
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