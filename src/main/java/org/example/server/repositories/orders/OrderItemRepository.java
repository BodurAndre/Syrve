package org.example.server.repositories.orders;

import org.example.server.models.orders.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<Item, Long> {
}
