package com.chandan.fruit.fmp.controller;

import com.chandan.fruit.fmp.model.FruitPrice;
import com.chandan.fruit.fmp.repo.FruitPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;      

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/fruit-month-price")
public class FruitMonthPriceController {

    private final FruitPriceRepository repository;
    private final Environment environment;

    @Autowired
    public FruitMonthPriceController(FruitPriceRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
    }
    
    @RestControllerAdvice
    class NotFoundAdvice {

        @ExceptionHandler(NoSuchElementException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public Map<String,String> handle(NoSuchElementException ex) {
            return Map.of("error", "fruit/month pair not found");
        }
    }

    // Fixed endpoint - removed the duplicate "/fruit-month-price" prefix
    @GetMapping("/fruit/{fruit}/month/{month}")
    public ResponseEntity<Map<String, Object>> getUnitPrice(
            @PathVariable("fruit") String fruit,
            @PathVariable("month") String month) {

        try {
            // Normalize input
            String normalizedFruit = fruit.trim().toLowerCase();
            String normalizedMonth = month.trim().toLowerCase();
            
            // Log the request for debugging
            System.out.println("Looking for fruit: " + normalizedFruit + ", month: " + normalizedMonth);
            
            // Find the fruit price
            var fruitPriceOpt = repository.findByFruitIgnoreCaseAndMonthIgnoreCase(
                    normalizedFruit, normalizedMonth);
                    
            if (fruitPriceOpt.isPresent()) {
                FruitPrice fruitPrice = fruitPriceOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("fruit", fruitPrice.getFruit());
                response.put("month", fruitPrice.getMonth());
                response.put("fmp", fruitPrice.getPrice());
                response.put("port", environment.getProperty("server.port"));
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Fruit/month combination not found");
                errorResponse.put("fruit", normalizedFruit);
                errorResponse.put("month", normalizedMonth);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in getUnitPrice: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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