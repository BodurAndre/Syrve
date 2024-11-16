package org.example.server.repositories;

import org.example.server.models.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<RestaurantInfo,Long> {
    Optional<RestaurantInfo> findByIdRestaurant(String idRestaurant);
    Optional<RestaurantInfo> findByApiLogin(String apiLogin);
}
