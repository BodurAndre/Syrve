package org.example.server.repositories;

import jakarta.transaction.Transactional;
import org.example.server.models.adress.Cities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CitiesRepository extends JpaRepository<Cities,Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cities", nativeQuery = true)
    void deleteAllCities();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE cities AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();

    @Query("SELECT c FROM Cities c WHERE c.cityId = :cityId")
    Cities findCityById(@Param("cityId") String cityId);
}
