package com.deli.controller;

import com.deli.dto.InventoryRequest;
import com.deli.dto.PriceRequest;
import com.deli.dto.SaleRequest;
import com.deli.model.DailyInventory;
import com.deli.model.Product;
import com.deli.model.Sale;
import com.deli.service.CremoService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CremoController {
    private final CremoService service;

    public CremoController(CremoService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }

    @GetMapping("/inventory/today")
    public DailyInventory inventory() {
        return service.getTodayInventory();
    }

    @PutMapping("/inventory/today")
    public DailyInventory updateInventory(@Valid @RequestBody InventoryRequest request) {
        return service.updateInventory(request);
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public Sale createSale(@Valid @RequestBody SaleRequest request) {
        return service.createSale(request);
    }

    @GetMapping("/reports/weekly")
    public Map<String, Object> weeklyReport() {
        return service.weeklyReport();
    }

    @PutMapping("/product/price")
    public Product updatePrice(@Valid @RequestBody PriceRequest request) {
        return service.updatePrice(request);
    }
}