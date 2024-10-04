package org.example.server.controllers;

import org.example.server.models.RestaurantInfo;
import org.example.server.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Controller
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @RequestMapping(value = "/viewRestaurant", method = RequestMethod.GET)
    public String viewRestaurant(Model model) {
        try {
            // Получение информации о ресторане через сервис
            RestaurantInfo restaurantInfo = restaurantService.getInfoRestaurant();

            // Добавление атрибутов в модель для отображения на странице
            model.addAttribute("idRestaurant", restaurantInfo.getIdRestaurant());
            model.addAttribute("apiLogin", restaurantInfo.getApiLogin()); // Если apiLogin является частью RestaurantInfo
            model.addAttribute("nameRestaurant", restaurantInfo.getNameRestaurant());
        } catch (NoSuchElementException e) {
            // Если данных нет, перенаправление на страницу ошибки
            return "test/restaurant";
        }

        // Возврат к представлению с информацией о ресторане
        return "test/restaurant";
    }

    @PostMapping("/updateApiLogin")
    public String updateApiLogin(@RequestParam("apiLogin") String newApiLogin) {
        // Обновляем значение apiLogin через сервис
        restaurantService.updateApiLogin(newApiLogin);

        // Перенаправляем пользователя обратно на страницу с информацией
        return "redirect:/viewRestaurant";
    }

    @GetMapping("/getNameRestaurant")
    @ResponseBody
    public String getNameRestaurant() {
        String nameRestaurant = restaurantService.getNameRestaurant();
        System.out.println("getNameRestaurant" + nameRestaurant);
        return nameRestaurant;
    }
}

