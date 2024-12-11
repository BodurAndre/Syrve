package org.example.server.repositories;


import jakarta.transaction.Transactional;
import org.example.server.models.adress.Cities;
import org.example.server.models.adress.Streets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreetsRepository extends JpaRepository<Streets,Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM streets", nativeQuery = true)
    void deleteAllStreets();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE streets AUTO_INCREMENT = 1", nativeQuery = true)
    void resetProductAutoIncrement();

    @Query("SELECT s FROM Streets s WHERE s.city.cityId = :cityId")
    List<Streets> findStreetsById(@Param("cityId") String cityId);

    @Query("SELECT s FROM Streets s WHERE s.streetId = :streetId")
    Streets findStreetById(@Param("streetId") String streetId);

}
