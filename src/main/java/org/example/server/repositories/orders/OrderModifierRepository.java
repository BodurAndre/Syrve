package org.example.server.repositories.orders;

import org.example.server.models.orders.OrderModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderModifierRepository extends JpaRepository<OrderModifier, Long> {
}
