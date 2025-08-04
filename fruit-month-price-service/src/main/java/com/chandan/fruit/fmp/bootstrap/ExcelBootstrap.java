package com.chandan.fruit.fmp.bootstrap;

import com.chandan.fruit.fmp.model.FruitPrice;
import com.chandan.fruit.fmp.repo.FruitPriceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelBootstrap {

    private final FruitPriceRepository repo;

    @Autowired
    public ExcelBootstrap(FruitPriceRepository repo) {
        this.repo = repo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importIfEmpty() throws Exception {

        if (repo.count() > 0) return;  // already imported

        try (InputStream in = getClass().getResourceAsStream("/FMP.xlsx");
             Workbook wb = new XSSFWorkbook(in)) {

            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);          // months in B..M

            List<FruitPrice> batch = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String fruit = row.getCell(0).getStringCellValue().trim().toLowerCase();

                for (int c = 1; c < header.getLastCellNum(); c++) {
                    Cell monthCell = header.getCell(c);
                    String month = monthCell.getStringCellValue().trim().toLowerCase();

                    Cell priceCell = row.getCell(c);
                    if (priceCell == null || priceCell.getCellType() != CellType.NUMERIC) continue;

                    BigDecimal price = BigDecimal.valueOf(priceCell.getNumericCellValue());
                    batch.add(new FruitPrice(null, fruit, month, price));
                }
            }
            repo.saveAll(batch);
            System.out.println("Imported " + batch.size() + " rows into MongoDB.");
        }
    }
}