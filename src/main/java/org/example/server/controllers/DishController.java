package org.example.server.controllers;

import org.example.server.DTO.DishDTO;
import org.example.server.DTO.ModifierDTO;
import org.example.server.DTO.ModifierGroupDTO;
import org.example.server.DTO.ProductDTO;
import org.example.server.models.products.DishModifier;
import org.example.server.models.products.Groups;
import org.example.server.models.products.Modifier;
import org.example.server.repositories.DishModifierRepository;
import org.example.server.repositories.DishRepository;
import org.example.server.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private DishModifierRepository dishModifierRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/dishes")
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
                    dishDTO.setAmount(1);
                    if (dish.getGroupId() != null) dishDTO.setGroupName(dish.getGroupId().getName());

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
                                groupDTO.setFreeOfChargeAmount(entry.getValue().stream()
                                        .mapToInt(DishModifier::getFreeOfChargeAmountAll).max().orElse(0));
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
                                    dto.setIdGroup(modifier.getGroupId().getIdGroup());
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

    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> productDTOs = productRepository.findAll().stream()
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setId(product.getIdProducts());
                    productDTO.setName(product.getName());
                    productDTO.setCode(product.getCode());
                    productDTO.setWeight(product.getMeasureUnit());
                    productDTO.setPrice(product.getPrice());
                    productDTO.setImageLinks(product.getImageLinks());
                    productDTO.setIsIncludedMenu(product.getIsIncludedMenu());
                    productDTO.setAmount(1);
                    if (product.getGroupId() != null) productDTO.setGroupName(product.getGroupId().getName());

                    return productDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }
}
