package org.example.server.repositories.orders;

import org.example.server.models.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.ipAddress = :ipAddress ORDER BY o.createdAt DESC")
    List<Order> findOrdersByIpAddress(@Param("ipAddress") String ipAddress);
}
