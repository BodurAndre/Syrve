package org.example.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.server.models.adress.Cities;
import org.example.server.models.adress.Streets;
import org.example.server.models.products.Groups;
import org.example.server.repositories.CitiesRepository;
import org.example.server.repositories.StreetsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
    public void saveStreetFromJson(JsonNode street, String cityID) throws JsonProcessingException {
        Cities cities = getCitiesById(cityID);
        log.info(cities.toString());
                street.forEach(citiesNode -> {
                    Streets newStreet = new Streets(
                    citiesNode.get("id").asText(),
                    citiesNode.get("name").asText(),
                    citiesNode.get("isDeleted").asBoolean(),
                    cities
            );
            streetsRepository.save(newStreet);
        });
    }

    private void clearExistingData() {
        streetsRepository.deleteAllStreets();
        streetsRepository.resetProductAutoIncrement();

        citiesRepository.deleteAllCities();
        citiesRepository.resetProductAutoIncrement();
    }

    private Cities getCitiesById(String id) {
        return citiesRepository.findCityById(id);
    }
}
