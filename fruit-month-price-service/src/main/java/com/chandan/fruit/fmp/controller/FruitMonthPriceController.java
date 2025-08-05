package com.chandan.fruit.fmp.controller;

import com.chandan.fruit.fmp.model.FruitPrice;
import com.chandan.fruit.fmp.repo.FruitPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;
import com.chandan.fruit.fmp.repo.FruitPriceRepository;
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

    @GetMapping("/fruit-month-price/fruit/{fruit}/month/{month}")
    public ResponseEntity<FruitPrice> getUnitPrice(
            @PathVariable String fruit,
            @PathVariable String month) {

        return repository.findByFruitIgnoreCaseAndMonthIgnoreCase(
                        fruit.trim(),
                        month.trim().toLowerCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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