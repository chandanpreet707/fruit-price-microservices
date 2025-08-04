package com.chandan.fruit.fmp.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "fruit_price")
@CompoundIndex(name = "fruit_month_idx", def = "{'fruit': 1, 'month': 1}", unique = true)
public class FruitPrice {
    
    @Id
    private ObjectId id;
    private String fruit;
    private String month;
    private BigDecimal price;
    
    // Default constructor
    public FruitPrice() {
    }
    
    // All args constructor
    public FruitPrice(ObjectId id, String fruit, String month, BigDecimal price) {
        this.id = id;
        this.fruit = fruit;
        this.month = month;
        this.price = price;
    }
    
    // Getters
    public ObjectId getId() {
        return id;
    }
    
    public String getFruit() {
        return fruit;
    }
    
    public String getMonth() {
        return month;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    // Setters
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public void setFruit(String fruit) {
        this.fruit = fruit;
    }
    
    public void setMonth(String month) {
        this.month = month;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}