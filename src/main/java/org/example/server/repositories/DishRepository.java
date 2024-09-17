package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.products.Dish;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface DishRepository extends JpaRepository<Dish,Long>  {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM dishes", nativeQuery = true)
    void deleteAllProducts();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE dishes AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();
}
