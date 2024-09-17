package org.example.server.service;

import org.example.server.models.products.Dish;
import org.example.server.models.products.DishModifier;
import org.example.server.repositories.DishModifierRepository;
import org.example.server.repositories.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    @Autowired
    private DishModifierRepository dishModifierRepository;
    @Autowired
    private DishRepository dishRepository;

    // Метод для получения всех модификаторов для конкретного блюда
    public List<DishModifier> getModifiersForDish(Dish dish) {
        return dishModifierRepository.findByDish(dish);
    }

    // Пример использования метода в сервисе
    public void printDishModifiers() {
        for(Dish ignored : dishRepository.findAll()) {
            List<DishModifier> modifiers = getModifiersForDish(ignored);
            for (DishModifier modifier : modifiers) {
                if (modifier.getModifier() != null) {
                    System.out.println("Modifier ID: " + modifier.getModifier().getId());
                    System.out.println("Modifier Name: " + modifier.getModifier().getName());
                } else if (modifier.getModifierDishID() != null) {
                    System.out.println("Modifier ID: " + modifier.getModifierDishID().getId());
                    System.out.println("Modifier Name: " + modifier.getModifierDishID().getName());
                }
                else {
                    System.out.println("Modifier ID: " + null);
                }
            }
        }
    }


}
