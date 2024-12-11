package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.products.Dish;
import org.example.server.models.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>  {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM products", nativeQuery = true)
    void deleteAllProducts();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE products AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();

    @Query("SELECT c FROM Product c WHERE c.idProducts = :productId")
    Product findProductById(@Param("productId") String productId);
}
