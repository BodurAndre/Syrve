package org.example.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.server.models.RestaurantInfo;
import org.example.server.models.TerminalGroup;
import org.example.server.repositories.RestaurantRepository;
import org.example.server.repositories.TerminalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private TerminalRepository terminalRepository;


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

    public RestaurantInfo getRestaurantById(String idRestaurant) {
        System.out.println(idRestaurant);
        return restaurantRepository.findByIdRestaurant(idRestaurant).orElseThrow(); // Если ничего не найдено, кидаем исключение
    }

    public void saveTerminalGroupFromJson(JsonNode terminalGroupFromJson) {
        System.out.println(terminalGroupFromJson);
        saveTerminalGroups(terminalGroupFromJson.get("terminalGroups"), true);

        // Обработка терминальных групп в спящем режиме
        saveTerminalGroups(terminalGroupFromJson.get("terminalGroupsInSleep"), false);
    }

    private void saveTerminalGroups(JsonNode terminalGroupsNode, boolean isActive) {
        System.out.println("True");
        if (terminalGroupsNode == null || !terminalGroupsNode.isArray()) {
            return; // Выходим, если данные отсутствуют или не в виде массива
        }
        System.out.println("Обработка");
        for (JsonNode terminalGroup : terminalGroupsNode) {
            for (JsonNode item : terminalGroup.get("items")) {
                // Получаем информацию о ресторане по organizationId
                String organizationId = item.get("organizationId").asText();
                RestaurantInfo restaurantInfo = getRestaurantById(organizationId);

                TerminalGroup terminalGroupItem = new TerminalGroup(
                        restaurantInfo,
                        item.get("id").asText(),
                        item.get("name").asText(),
                        item.get("address").asText(),
                        item.get("timeZone").asText(),
                        isActive
                );
                terminalRepository.save(terminalGroupItem);
                System.out.println(terminalGroupItem.getIdRestaurant());
            }
        }
    }
}

