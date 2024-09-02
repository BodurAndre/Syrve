package org.example.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.server.models.products.*;
import org.example.server.repositories.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ModifierRepository modifierRepository;
    private final DishRepository dishRepository;
    private final DishModifierRepository dishModifierRepository;
    private final GroupsRepository groupsRepository;

    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, ModifierRepository modifierRepository, DishRepository dishRepository, DishModifierRepository dishModifierRepository, ObjectMapper objectMapper, GroupsRepository groupsRepository) {
        this.productRepository = productRepository;
        this.modifierRepository = modifierRepository;
        this.dishRepository = dishRepository;
        this.dishModifierRepository = dishModifierRepository;
        this.objectMapper = objectMapper;
        this.groupsRepository = groupsRepository;
    }

    @Transactional
    public void saveProductsFromJson(String json) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(json);
        JsonNode productsNode = rootNode.get("groups");


        productsNode.forEach(productNode -> {
            Groups groups = createGroups(productNode);
            groupsRepository.save(groups);
        });


        Map<String, Dish> dishMap = new HashMap<>();
        Map<String, Modifier> modifierMap = new HashMap<>();

        productsNode = rootNode.get("products");
        clearExistingData();

        productsNode.forEach(productNode -> {
            String type = productNode.get("type").asText();
            switch (type) {
                case "Modifier":
                    Modifier modifier = createModifier(productNode);
                    modifierMap.put(modifier.getProductId(), modifier);
                    modifierRepository.save(modifier);
                    break;
                case "Good":
                    Product product = createProduct(productNode);
                    productRepository.save(product);
                    break;
                case "Dish":
                    Dish dish = createDish(productNode);
                    dishMap.put(dish.getProductId(), dish);
                    dishRepository.save(dish);
                    break;
            }
        });

        productsNode.forEach(productNode -> {
            if ("Dish".equals(productNode.get("type").asText())) {
                Dish dish = dishMap.get(productNode.get("id").asText());
                saveDishModifiers(productNode, dish, modifierMap, dishMap);
            }
        });
    }

    private void clearExistingData() {
        dishModifierRepository.deleteAllProducts();
        dishModifierRepository.resetProductAutoIncrement();

        dishRepository.deleteAllProducts();
        dishRepository.resetProductAutoIncrement();

        modifierRepository.deleteAllProducts();
        modifierRepository.resetProductAutoIncrement();

        productRepository.deleteAllProducts();
        productRepository.resetProductAutoIncrement();
    }

    private Groups createGroups(JsonNode productNode) {
        return new Groups(
                productNode.get("id").asText(),
                productNode.get("parentGroup").asText(),
                productNode.get("name").asText(),
                productNode.get("isDeleted").asBoolean(),
                productNode.get("isIncludedInMenu").asBoolean(),
                productNode.get("isGroupModifier").asBoolean()
        );
    }

    private Modifier createModifier(JsonNode productNode) {
        return new Modifier(
                productNode.get("id").asText(),
                productNode.get("name").asText(),
                productNode.get("groupId").asText(),
                productNode.get("code").asText(),
                productNode.get("sizePrices").get(0).get("price").get("currentPrice").asDouble()
        );
    }

    private Product createProduct(JsonNode productNode) {
        return new Product(
                productNode.get("id").asText(),
                productNode.get("name").asText(),
                productNode.get("groupId").asText(),
                productNode.get("code").asText(),
                productNode.get("measureUnit").asText(),
                productNode.get("sizePrices").get(0).get("price").get("currentPrice").asDouble(),
                productNode.get("sizePrices").get(0).get("price").get("isIncludedInMenu").asBoolean()
        );
    }

    private Dish createDish(JsonNode productNode) {
        return new Dish(
                productNode.get("id").asText(),
                productNode.get("name").asText(),
                productNode.get("groupId").asText(),
                productNode.get("code").asText(),
                productNode.get("sizePrices").get(0).get("price").get("currentPrice").asDouble(),
                productNode.get("sizePrices").get(0).get("price").get("isIncludedInMenu").asBoolean(),
                productNode.get("measureUnit").asText()
        );
    }

    private void saveDishModifiers(JsonNode productNode, Dish dish, Map<String, Modifier> modifierMap, Map<String, Dish> dishMap) {
        saveGroupModifiers(productNode.get("groupModifiers"), dish, modifierMap, dishMap);
        saveModifiers(productNode.get("modifiers"), dish, modifierMap, dishMap);
    }

    private void saveGroupModifiers(JsonNode groupModifiersNode, Dish dish, Map<String, Modifier> modifierMap, Map<String, Dish> dishMap) {
        groupModifiersNode.forEach(groupModifierNode -> {
            int minAmount = groupModifierNode.get("minAmount").asInt();
            int maxAmount = groupModifierNode.get("maxAmount").asInt();
            int freeOfChargeAmountAll = groupModifierNode.get("freeOfChargeAmount").asInt();

            groupModifierNode.get("childModifiers").forEach(childModifierNode -> {
                DishModifier dishModifier = createDishModifier(childModifierNode, dish, modifierMap, dishMap);
                dishModifier.setMinQuantity(minAmount);
                dishModifier.setMaxQuantity(maxAmount);
                dishModifier.setFreeOfChargeAmountAll(freeOfChargeAmountAll);
                dishModifier.setGroupModifier(true);
                dishModifierRepository.save(dishModifier);
            });
        });
    }

    private void saveModifiers(JsonNode modifiersNode, Dish dish, Map<String, Modifier> modifierMap, Map<String, Dish> dishMap) {
        modifiersNode.forEach(modifierNode -> {
            DishModifier dishModifier = createDishModifier(modifierNode, dish, modifierMap, dishMap);
            dishModifier.setGroupModifier(false);
            dishModifierRepository.save(dishModifier);
        });
    }

    private DishModifier createDishModifier(JsonNode modifierNode, Dish dish, Map<String, Modifier> modifierMap, Map<String, Dish> dishMap) {
        DishModifier dishModifier = new DishModifier();
        dishModifier.setDish(dish);
        String modifierId = modifierNode.get("id").asText();
        if (modifierMap.containsKey(modifierId)) {
            dishModifier.setModifier(modifierMap.get(modifierId));
        } else if (dishMap.containsKey(modifierId)) {
            dishModifier.setModifierDishID(dishMap.get(modifierId));
        }
        dishModifier.setMinQuantity(modifierNode.get("minAmount").asInt());
        dishModifier.setMaxQuantity(modifierNode.get("maxAmount").asInt());
        dishModifier.setDefaultQuantity(modifierNode.get("defaultAmount").asInt());
        dishModifier.setFreeOfChargeAmount(modifierNode.get("freeOfChargeAmount").asInt());
        return dishModifier;
    }


    public List<Product> listProduct() {
       return productRepository.findAll();
    }

    public List<Dish> listDish() {
        return dishRepository.findAll();
    }

}
