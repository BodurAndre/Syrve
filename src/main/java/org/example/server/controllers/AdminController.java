package org.example.server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.example.server.DTO.Admin.CityWithStreetsDTO;
import org.example.server.DTO.Admin.Order.OrderAdminDTO;
import org.example.server.DTO.Admin.StreetDTO;
import org.example.server.DTO.Admin.UserDTO;
import org.example.server.models.RestaurantInfo;
import org.example.server.models.User;
import org.example.server.models.adress.Cities;
import org.example.server.models.adress.Streets;
import org.example.server.models.orders.Order;
import org.example.server.repositories.CitiesRepository;
import org.example.server.repositories.StreetsRepository;
import org.example.server.service.OrderService;
import org.example.server.service.RestaurantService;
import org.example.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AdminController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(PasswordEncoder passwordEncoder,
                           RestaurantService restaurantService,
                           OrderService orderService,
                           UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping("/admin/updateApiLogin")
    public ResponseEntity<String> updateApiLogin(
            @RequestParam("apiLogin") String newApiLogin,
            @RequestParam("emailRestaurant") String emailRestaurant,
            @RequestParam("phoneRestaurant") String phoneRestaurant,
            @RequestParam("addressRestaurant") String addressRestaurant) {

        if (newApiLogin == null || newApiLogin.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("API Login не может быть пустым.");
        }

        // Обновление данных через сервис
        restaurantService.updateApiLogin(newApiLogin, emailRestaurant, phoneRestaurant, addressRestaurant);
        return ResponseEntity.ok("Данные успешно обновлены.");
    }



    @GetMapping("/restaurant/getNameRestaurant")
    @ResponseBody
    public ResponseEntity<?> getNameRestaurant() {
        try {
            RestaurantInfo restaurantInfo = restaurantService.getInfoRestaurant();
            Map<String, Object> response = new HashMap<>();
            System.out.println(restaurantInfo.getEmailRestaurant());
            response.put("nameRestaurant", restaurantInfo.getNameRestaurant());
            response.put("emailRestaurant", restaurantInfo.getEmailRestaurant());
            response.put("phoneRestaurant", restaurantInfo.getPhoneRestaurant());
            response.put("addressRestaurant", restaurantInfo.getAddressRestaurant());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RequestMapping(value = "/admin/viewRestaurant", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> viewRestaurant() {
        try {
            RestaurantInfo restaurantInfo = restaurantService.getInfoRestaurant();

            Map<String, Object> response = new HashMap<>();
            response.put("idRestaurant", restaurantInfo.getIdRestaurant());
            response.put("apiLogin", restaurantInfo.getApiLogin());
            response.put("nameRestaurant", restaurantInfo.getNameRestaurant());
            response.put("emailRestaurant", restaurantInfo.getEmailRestaurant());
            response.put("phoneRestaurant", restaurantInfo.getPhoneRestaurant());
            response.put("addressRestaurant", restaurantInfo.getAddressRestaurant());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @RestController
    @RequestMapping("/admin/api/locations")
    public class LocationController {
        @Autowired
        private CitiesRepository citiesRepository;

        @Autowired
        private StreetsRepository streetsRepository;

        @GetMapping("/all")
        public ResponseEntity<List<CityWithStreetsDTO>> getAllCitiesWithStreets() {
            List<Cities> cities = citiesRepository.findAll();
            List<CityWithStreetsDTO> result = new ArrayList<>();

            for (Cities city : cities) {
                CityWithStreetsDTO dto = new CityWithStreetsDTO();
                dto.setCityName(city.getCity());
                dto.setCityId(city.getCityId());
                dto.setDeleted(city.isDeleted());

                List<Streets> streets = streetsRepository.findStreetsById(city.getCityId());
                List<StreetDTO> streetDTOs = streets.stream()
                        .map(street -> {
                            StreetDTO streetDTO = new StreetDTO();
                            streetDTO.setStreetName(street.getName());
                            streetDTO.setStreetId(street.getStreetId());
                            streetDTO.setDeleted(street.isDeleted());
                            return streetDTO;
                        })
                        .collect(Collectors.toList());

                dto.setStreets(streetDTOs);
                result.add(dto);
            }

            return ResponseEntity.ok(result);
        }
    }

    @RequestMapping(value = "/admin/viewOrder", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> viewOrder() {
        try {
            List<OrderAdminDTO> Order = orderService.getAllorder();
            return ResponseEntity.ok(Order);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }




    // Простые запросы

    @GetMapping(value = "/admin/info")
    public String home(){
        return "/admin/restaurant-info";
    }

    @GetMapping(value = "/admin/")
    public String test(){
        return "/admin/productlist";
    }

    @GetMapping("/admin/orders")
    public String orderStatus() {
        return "test/OrderStatus";
    }

    @GetMapping("/admin/address")
    public String address() {
        return "/admin/cities-streets";
    }

    @GetMapping("/admin/products")
    public String products() {
        return "/admin/productlist";
    }

    @GetMapping("/admin/salesList")
    public String salesList() {
        return "/admin/salesList";
    }

    @GetMapping("/admin/myProfile")
    public String myProfile() {
        return "/admin/profile";
    }

    //----------------------------------------------------------------------

    @PostMapping("/admin/editStatus")
    public ResponseEntity<String> processOrder(@RequestBody String orderId) {
        Order order = orderService.getOrder(orderId);
        orderService.editStatusOrder(order, "Waiting");
        return ResponseEntity.ok("Статус изменен");
    }

    @GetMapping(value = "/admin/order/{id}")
    public String getOrderPage(@PathVariable("id") String id) {
        return "admin/sales-details"; // Просто возвращаем HTML, без передачи данных
    }

    @GetMapping(value = "/admin/api/order/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<OrderAdminDTO> getOrderData(@PathVariable("id") String id) {
        OrderAdminDTO order = orderService.getOrderByID(id);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping(value = "/admin/getProfile")
    @ResponseBody
    public ResponseEntity<UserDTO> getProfile() {
        User user = userService.getAuthenticatedUser();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setGender(user.getGender());
        userDTO.setDateOfBirth(user.getDateOfBirth());
        userDTO.setRole(user.getRole());
        userDTO.setPhone(user.getPhone());
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/admin/check-password")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkPassword(@RequestBody Map<String, String> request) {
        // Получаем введенный старый пароль из запроса
        String oldPassword = request.get("oldPassword");
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false)); // Если пароль не передан
        }
        // Получаем аутентифицированного пользователя
        User user = userService.getAuthenticatedUser();
        // Проверяем пароль
        boolean isPasswordValid = passwordEncoder.matches(oldPassword, user.getPassword());

        // Формируем и возвращаем ответ
        return ResponseEntity.ok(Map.of("valid", isPasswordValid));
    }

    @PostMapping("/admin/update-profile")
    @ResponseBody
    public ResponseEntity<String> updateProfile(@RequestBody Map<String, String> updatedData) {
        User user = userService.getAuthenticatedUser();

        // Обновляем только изменённые поля
        updatedData.forEach((key, value) -> {
            switch (key) {
                case "firstName":
                    user.setFirstName(value);
                    break;
                case "lastName":
                    user.setLastName(value);
                    break;
                case "email":
                    user.setEmail(value);
                    break;
                case "phone":
                    user.setPhone(value);
                    break;
                case "newPassword":
                    String encodedPassword = passwordEncoder.encode(value);
                    user.setPassword(encodedPassword);
                    break;
            }
        });

        // Сохраняем изменения
        userService.saveUser(user);
        return ResponseEntity.ok("Profile updated successfully");
    }

}
