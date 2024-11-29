package org.example.server.repositories.orders;

import org.example.server.models.orders.OrderInfo;
import org.example.server.models.orders.Response;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderResponseRepository extends JpaRepository<Response, Long> {
}
