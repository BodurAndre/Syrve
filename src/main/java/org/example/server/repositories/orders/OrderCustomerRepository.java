package org.example.server.repositories.orders;

import org.example.server.models.orders.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderCustomerRepository extends JpaRepository<Customer, Long> {
}
