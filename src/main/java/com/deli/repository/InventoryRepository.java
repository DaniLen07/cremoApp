package com.deli.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.deli.model.DailyInventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<DailyInventory, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DailyInventory> findByProductIdAndInventoryDate(Long productId, LocalDate inventoryDate);
}