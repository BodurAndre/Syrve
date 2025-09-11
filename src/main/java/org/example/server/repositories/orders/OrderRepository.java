package org.example.server.repositories.orders;

import org.example.server.models.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.id = :id ORDER BY o.createdAt DESC")
    Order findOrdersById(@Param("id") String id);
    
    // Методы для дашборда
    long countByStatus(String status);
    
    @Query("SELECT SUM(CAST(o.totalPrice AS double)) FROM Order o WHERE o.status = :status")
    Double sumTotalPriceByStatus(@Param("status") String status);
    
    List<Order> findTop10ByOrderByCreatedAtDesc();
}
