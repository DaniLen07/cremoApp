package com.deli.repository;

import com.deli.model.DailyInventory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<DailyInventory, Long> {
    Optional<DailyInventory> findByProductIdAndInventoryDate(Long productId, LocalDate inventoryDate);
}