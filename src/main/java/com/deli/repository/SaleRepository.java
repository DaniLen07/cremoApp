package com.deli.repository;

import com.deli.model.Sale;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop20ByOrderByCreatedAtDesc();

    List<Sale> findBySaleDateBetweenOrderByCreatedAtDesc(LocalDate start, LocalDate end);

    @Query("select coalesce(sum(s.quantity), 0) from Sale s where s.saleDate = :date")
    Integer totalUnitsByDate(LocalDate date);

    @Query("select coalesce(sum(s.total), 0) from Sale s where s.saleDate = :date")
    java.math.BigDecimal totalAmountByDate(LocalDate date);
}