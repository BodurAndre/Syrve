package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.products.Dish;
import org.example.server.models.products.DishModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishModifierRepository extends JpaRepository<DishModifier,Long>  {

    List<DishModifier> findByDish(Dish dish);

    @Query("SELECT m FROM DishModifier m WHERE m.dish.id = :id")
    List<DishModifier> findByDishId(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM dishmodifier", nativeQuery = true)
    void deleteAllProducts();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE dishmodifier AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();
}
