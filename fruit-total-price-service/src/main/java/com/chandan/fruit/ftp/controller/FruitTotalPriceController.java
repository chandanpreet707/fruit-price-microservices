package com.chandan.fruit.ftp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fruit-total-price")
public class FruitTotalPriceController {

    private static final Logger log = LoggerFactory.getLogger(FruitTotalPriceController.class);
    
    private final WebClient.Builder webClientBuilder;
    private final Environment environment;

    @Autowired
    public FruitTotalPriceController(WebClient.Builder webClientBuilder, Environment environment) {
        this.webClientBuilder = webClientBuilder;
        this.environment = environment;
    }

    @GetMapping("/fruit/{fruitName}/month/{monthName}/quantity/{quantity}")
    public ResponseEntity<Map<String, Object>> getTotalPrice(
            @PathVariable("fruitName") String fruitName,
            @PathVariable("monthName") String monthName,
            @PathVariable("quantity") BigDecimal quantity) {
        
        // Validate quantity
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid quantity");
            errorResponse.put("message", "Quantity must be greater than 0");
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            // Call FMP service
            String fmpUrl = "http://localhost:8000/fruit-month-price/fruit/{fruit}/month/{month}";
            
            Map<String, Object> fmpResponse = webClientBuilder.build()
                    .get()
                    .uri(fmpUrl, fruitName, monthName)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(Map.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("FMP service error"))))
                    .bodyToMono(Map.class)
                    .block();

            if (fmpResponse != null && fmpResponse.containsKey("fmp")) {
                BigDecimal unitPrice = new BigDecimal(fmpResponse.get("fmp").toString());
                BigDecimal totalPrice = unitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

                Map<String, Object> response = new HashMap<>();
                response.put("id", 10001); // Static ID as per requirement
                response.put("fruit", fmpResponse.get("fruit"));
                response.put("month", fmpResponse.get("month"));
                response.put("fmp", unitPrice);
                response.put("quantity", quantity);
                response.put("totalPrice", totalPrice);
                response.put("environment", environment.getProperty("server.port") + " instance-id");
                
                log.info("Calculated total price for {} kg of {} in {}: {}", 
                        quantity, fruitName, monthName, totalPrice);
                
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Invalid response from FMP service");
            }
            
        } catch (Exception e) {
            log.error("Error calling FMP service: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service unavailable");
            errorResponse.put("message", "Could not retrieve fruit price from FMP service");
            errorResponse.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "fruit-total-price-service");
        response.put("port", environment.getProperty("server.port"));
        return ResponseEntity.ok(response);
    }
}