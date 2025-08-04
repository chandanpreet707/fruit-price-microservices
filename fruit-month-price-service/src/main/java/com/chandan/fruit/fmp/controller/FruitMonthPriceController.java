package com.chandan.fruit.fmp.controller;

import com.chandan.fruit.fmp.model.FruitPrice;
import com.chandan.fruit.fmp.repo.FruitPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fruit-month-price")
public class FruitMonthPriceController {

    private final FruitPriceRepository repository;
    private final Environment environment;

    @Autowired
    public FruitMonthPriceController(FruitPriceRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }

    @GetMapping("/fruit/{fruitName}/month/{monthName}")
    public ResponseEntity<Map<String, Object>> getFruitPrice(
            @PathVariable String fruitName,
            @PathVariable String monthName) {
        
        return repository.findByFruitIgnoreCaseAndMonthIgnoreCase(fruitName, monthName)
                .map(fruitPrice -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", 10001); // Static ID as per requirement
                    response.put("fruit", fruitPrice.getFruit());
                    response.put("month", fruitPrice.getMonth());
                    response.put("fmp", fruitPrice.getPrice());
                    response.put("environment", environment.getProperty("server.port") + " instance-id");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Fruit price not found");
                    errorResponse.put("message", String.format("No price found for fruit '%s' in month '%s'", fruitName, monthName));
                    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "fruit-month-price-service");
        response.put("port", environment.getProperty("server.port"));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug/all")
    public ResponseEntity<Map<String, Object>> debugAll() {
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", repository.count());
        response.put("samples", repository.findAll().stream().limit(5).toList());
        return ResponseEntity.ok(response);
    }
}