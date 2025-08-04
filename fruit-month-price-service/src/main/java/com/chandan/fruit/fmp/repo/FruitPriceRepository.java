package com.chandan.fruit.fmp.repo;

import com.chandan.fruit.fmp.model.FruitPrice;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FruitPriceRepository
        extends MongoRepository<FruitPrice, ObjectId> {

    Optional<FruitPrice> findByFruitIgnoreCaseAndMonthIgnoreCase(
            String fruit, String month);
}
