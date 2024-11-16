package org.example.server.models;

import com.fasterxml.jackson.databind.JsonNode;

public class OrderRequest {
    public String organizationId;
    public String terminalGroupId;
    public CreateOrderSettings createOrderSettings;
    public JsonNode order;  // Используем JsonNode для гибкости

    public static class CreateOrderSettings {
        public String mode = "Async"; // По умолчанию
    }
}
