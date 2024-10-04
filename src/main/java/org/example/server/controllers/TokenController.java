package org.example.server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.server.models.products.Dish;
import org.example.server.models.products.DishModifier;
import org.example.server.models.products.DishWithModifiers;
import org.example.server.models.products.Product;
import org.example.server.repositories.DishModifierRepository;
import org.example.server.repositories.DishRepository;
import org.example.server.repositories.ModifierRepository;
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


    private final ProductService productService;
    private final DishModifierRepository dishModifierRepository;
    private final RestaurantService restaurantService;

    public TokenController(ProductService productService, DishModifierRepository dishModifierRepository, RestaurantService restaurantService) {
        this.productService = productService;
        this.dishModifierRepository = dishModifierRepository;
        this.restaurantService = restaurantService;
    }

    private static final String TOKEN_URL = "https://api-ru.iiko.services/api/1/access_token";
    private static final String ORGANIZATIONS_URL = "https://api-ru.iiko.services/api/1/organizations";
    private static final String NOMENCLATURE_URL = "https://api-ru.iiko.services/api/1/nomenclature";

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

    @GetMapping("/saveProducts")
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
            return "redirect:/saveProducts";
        }

        if (idRestaurant == null || idRestaurant.isEmpty()) {
            getOrganization(); // Initialize idRestaurant if not found
            return "redirect:/saveProducts";
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
                return "redirect:/saveProducts";
            }
            if (e.getRawStatusCode() == 400){
                getOrganization();
                return "redirect:/saveProducts";
            }
            else {
                model.addAttribute("Error", "Ошибка при сохранении продуктов: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка обработки JSON", e);
        }

        return "test/tokenResult";
    }

    @RequestMapping(value = "/resetToken", method = RequestMethod.POST)
    public String resetToken() {
        token = null;
        return "redirect:/viewRestaurant";
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
}
