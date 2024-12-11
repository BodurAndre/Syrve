package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.adress.Cities;
import org.example.server.models.adress.Streets;
import org.example.server.models.products.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModifierRepository extends JpaRepository<Modifier,Long>  {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM modifiers", nativeQuery = true)
    void deleteAllProducts();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE modifiers AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();

    @Query("SELECT c FROM Modifier c WHERE c.productId = :productId")
    Modifier findModifierById(@Param("productId") String productId);
}
