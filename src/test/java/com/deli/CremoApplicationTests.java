package com.deli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.deli.dto.InventoryRequest;
import com.deli.service.CremoService;

@SpringBootTest
class CremoApplicationTests {
    @Autowired
    private CremoService cremoService;

    @Test
    void contextLoads() {
    }

    @Test
    void dailyInventoryCanBeReadAndUpdatedWithoutLosingToppings() {
        cremoService.updateInventory(new InventoryRequest(37, 5, 3, 2));

        var inventory = cremoService.getTodayInventory();
        assertEquals(37, inventory.getAvailableQuantity());
        assertEquals(5, inventory.getArequipeQuantity());
        assertEquals(3, inventory.getPowderedMilkQuantity());
        assertEquals(2, inventory.getRaisinsQuantity());
    }
}