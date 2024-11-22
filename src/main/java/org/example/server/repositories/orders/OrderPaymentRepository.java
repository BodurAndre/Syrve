package org.example.server.repositories.orders;

import org.example.server.models.orders.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPaymentRepository extends JpaRepository<Payment, Long> {
}
