package org.example.server.service;

import org.example.server.models.RestaurantInfo;
import org.example.server.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public RestaurantInfo saveOrUpdateRestaurant(String idRestaurant, String nameRestaurant, String apiLogin) {
        Optional<RestaurantInfo> existingRestaurant = restaurantRepository.findByApiLogin(apiLogin);
        RestaurantInfo restaurant;

        restaurant = existingRestaurant.get();
        restaurant.setNameRestaurant(nameRestaurant);
        restaurant.setIdRestaurant(idRestaurant);

        return restaurantRepository.save(restaurant);
    }

    public RestaurantInfo getInfoRestaurant() {
        return restaurantRepository.findAll().stream().findFirst().orElseThrow(); // Если ничего не найдено, кидаем исключение
    }

    public RestaurantInfo savApiLogin(String apiLogin) {
        // Создаем новый объект RestaurantInfo с введённым apiLogin
        RestaurantInfo newRestaurant = new RestaurantInfo();
        newRestaurant.setApiLogin(apiLogin);

        // Сохраняем новый объект в базе данных
        return restaurantRepository.save(newRestaurant);
    }

    public void updateApiLogin(String newApiLogin) {
        try {
            // Пытаемся получить существующую запись о ресторане
            RestaurantInfo restaurantInfo = getInfoRestaurant();

            // Если запись найдена, обновляем её apiLogin
            restaurantInfo.setApiLogin(newApiLogin);
            restaurantRepository.save(restaurantInfo);
        } catch (NoSuchElementException e) {
            // Если записи нет, создаем новую с переданным apiLogin
            savApiLogin(newApiLogin);
        }
    }

    public String getApiLogin() {
        // Пытаемся получить первую запись ресторана из базы данных
        RestaurantInfo restaurantInfo = restaurantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("API login not found"));

        // Возвращаем значение apiLogin
        return restaurantInfo.getApiLogin();
    }
    public String getIdRestaurant() {
        // Пытаемся получить первую запись ресторана из базы данных
        RestaurantInfo restaurantInfo = restaurantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Id Restaurant not found"));

        return restaurantInfo.getIdRestaurant();
    }
    public String getNameRestaurant() {
        // Пытаемся получить первую запись ресторана из базы данных
        RestaurantInfo restaurantInfo = restaurantRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Id Restaurant not found"));

        return restaurantInfo.getNameRestaurant();
    }


}

