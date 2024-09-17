package org.example.server.controllers;

import ch.qos.logback.core.model.Model;
import org.example.server.DTO.DishDTO;
import org.example.server.DTO.ModifierDTO;
import org.example.server.DTO.ModifierGroupDTO;
import org.example.server.models.products.DishModifier;
import org.example.server.models.products.Groups;
import org.example.server.models.products.Modifier;
import org.example.server.repositories.DishModifierRepository;
import org.example.server.repositories.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishModifierRepository dishModifierRepository;

    @GetMapping("/modifiers")
    public ResponseEntity<List<DishDTO>> getAllDishesWithModifiers() {
        List<DishDTO> dishDTOs = dishRepository.findAll().stream()
                .map(dish -> {
                    DishDTO dishDTO = new DishDTO();
                    dishDTO.setId(dish.getProductId());
                    dishDTO.setName(dish.getName());
                    dishDTO.setPrice(dish.getPrice());
                    dishDTO.setIsIncludedMenu(dish.getIsIncludedMenu());
                    dishDTO.setWeight(dish.getWeight());
                    dishDTO.setImageLinks(dish.getImageLinks());
                    dishDTO.setCode(dish.getCode());

                    // Группировка модификаторов по их группам
                    Map<Groups, List<DishModifier>> groupedModifiers = dishModifierRepository.findByDish(dish).stream()
                            .filter(DishModifier::isGroupModifier) // Фильтруем только групповые модификаторы
                            .collect(Collectors.groupingBy(dishModifier -> dishModifier.getModifier().getGroupId()));

                    // Преобразуем группы в ModifierGroupDTO
                    List<ModifierGroupDTO> modifierGroupDTOs = groupedModifiers.entrySet().stream()
                            .map(entry -> {
                                ModifierGroupDTO groupDTO = new ModifierGroupDTO();
                                Groups group = entry.getKey();
                                groupDTO.setName(group.getName());
                                groupDTO.setMaxQuantity(entry.getValue().stream()
                                        .mapToInt(DishModifier::getMaxQuantity).max().orElse(0)); // Получаем максимальное значение maxQuantity для группы

                                List<ModifierDTO> modifiers = entry.getValue().stream()
                                        .map(dishModifier -> {
                                            ModifierDTO dto = new ModifierDTO();
                                            Modifier modifier = dishModifier.getModifier();
                                            if (modifier != null) {
                                                dto.setId(modifier.getProductId());
                                                dto.setName(modifier.getName());
                                                dto.setDefaultQuantity(dishModifier.getDefaultQuantity());
                                                dto.setMinQuantity(dishModifier.getMinQuantity());
                                                dto.setMaxQuantity(dishModifier.getMaxQuantity());
                                                dto.setFreeOfChargeAmount(dishModifier.getFreeOfChargeAmount());
                                                dto.setFreeOfChargeAmountAll(dishModifier.getFreeOfChargeAmountAll());
                                                dto.setGroupModifier(dishModifier.isGroupModifier());
                                                dto.setCurrentPrice(modifier.getCurrentPrice());
                                                dto.setNameGroup(modifier.getGroupId().getName());
                                                dto.setIdGroup(modifier.getGroupId().getIdGroup());
                                            }
                                            return dto;
                                        })
                                        .collect(Collectors.toList());

                                groupDTO.setModifiers(modifiers);
                                return groupDTO;
                            })
                            .collect(Collectors.toList());

                    // Обычные модификаторы
                    List<ModifierDTO> regularModifiers = dishModifierRepository.findByDish(dish).stream()
                            .filter(dishModifier -> !dishModifier.isGroupModifier()) // Фильтруем обычные модификаторы
                            .map(dishModifier -> {
                                ModifierDTO dto = new ModifierDTO();
                                Modifier modifier = dishModifier.getModifier();
                                if (modifier != null) {
                                    dto.setId(modifier.getProductId());
                                    dto.setName(modifier.getName());
                                    dto.setDefaultQuantity(dishModifier.getDefaultQuantity());
                                    dto.setMinQuantity(dishModifier.getMinQuantity());
                                    dto.setMaxQuantity(dishModifier.getMaxQuantity());
                                    dto.setFreeOfChargeAmount(dishModifier.getFreeOfChargeAmount());
                                    dto.setFreeOfChargeAmountAll(dishModifier.getFreeOfChargeAmountAll());
                                    dto.setGroupModifier(dishModifier.isGroupModifier());
                                    dto.setCurrentPrice(modifier.getCurrentPrice());
                                }
                                else {
                                    dto.setId(dishModifier.getModifierDishID().getProductId());
                                    dto.setName(dishModifier.getModifierDishID().getName());
                                    dto.setDefaultQuantity(dishModifier.getDefaultQuantity());
                                    dto.setMinQuantity(dishModifier.getMinQuantity());
                                    dto.setMaxQuantity(dishModifier.getMaxQuantity());
                                    dto.setFreeOfChargeAmount(dishModifier.getFreeOfChargeAmount());
                                    dto.setFreeOfChargeAmountAll(dishModifier.getFreeOfChargeAmountAll());
                                    dto.setGroupModifier(dishModifier.isGroupModifier());
                                    dto.setCurrentPrice(dishModifier.getModifierDishID().getPrice());
                                }
                                return dto;
                            })
                            .collect(Collectors.toList());

                    dishDTO.setModifierGroups(modifierGroupDTOs);
                    dishDTO.setModifiers(regularModifiers);
                    return dishDTO;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dishDTOs);
    }


}
