package org.example.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.example.server.models.adress.Cities;
import org.example.server.models.adress.Streets;
import org.example.server.repositories.CitiesRepository;
import org.example.server.repositories.StreetsRepository;
import org.springframework.stereotype.Service;

@Service
public class AdressService {

    private final CitiesRepository citiesRepository;
    private final StreetsRepository streetsRepository;

    public AdressService(CitiesRepository citiesRepository, StreetsRepository streetsRepository) {
        this.citiesRepository = citiesRepository;
        this.streetsRepository = streetsRepository;
    }

    @Transactional
    public void saveCityFromJson(JsonNode cities) throws JsonProcessingException {
        clearExistingData();
        cities.forEach(citiesNode -> {
            Cities city = new Cities(
                    citiesNode.get("id").asText(),
                    citiesNode.get("name").asText(),
                    citiesNode.get("isDeleted").asBoolean()
            );
            citiesRepository.save(city);
        });
    }

    @Transactional
    public void saveStreetFromJson(JsonNode cities) throws JsonProcessingException {
        cities.forEach(citiesNode -> {
            Streets street = new Streets(
                    citiesNode.get("id").asText(),
                    citiesNode.get("name").asText(),
                    citiesNode.get("isDeleted").asBoolean()
            );
            streetsRepository.save(street);
        });
    }

    private void clearExistingData() {
        citiesRepository.deleteAllCities();
        citiesRepository.resetProductAutoIncrement();

        streetsRepository.deleteAllStreets();
        streetsRepository.resetProductAutoIncrement();
    }
}
