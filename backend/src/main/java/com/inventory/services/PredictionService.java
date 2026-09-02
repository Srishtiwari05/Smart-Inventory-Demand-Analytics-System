package com.inventory.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PredictionService {

    private static final String ML_SERVICE_URL = "http://localhost:5000/predict/";
    private final HttpClient httpClient;

    public PredictionService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Calls the Python ML Service to get a demand prediction.
     * @param productId The product ID to predict demand for.
     * @return JSON string containing the prediction, or null if it fails.
     */
    public String getDemandPrediction(int productId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ML_SERVICE_URL + productId))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("ML Service returned status code: " + response.statusCode());
                return "{\"error\": \"Failed to retrieve prediction. ML Service returned " + response.statusCode() + "\"}";
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Python ML Service: " + e.getMessage());
            return "{\"error\": \"Connection to ML Service failed. Is it running on port 5000?\"}";
        }
    }
}
