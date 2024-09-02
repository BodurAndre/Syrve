package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.products.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
