package com.deli.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.deli.dto.SellerDailySummary;
import com.deli.model.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @Query("select s from Sale s order by s.createdAt desc, s.id desc")
    List<Sale> findTop20ByOrderByCreatedAtDesc();

    @Query("select s from Sale s where s.saleDate between :start and :end order by s.createdAt desc, s.id desc")
    List<Sale> findBySaleDateBetweenOrderByCreatedAtDesc(LocalDate start, LocalDate end);

    @Query("select coalesce(sum(s.quantity), 0) from Sale s where s.saleDate = :date")
    Integer totalUnitsByDate(LocalDate date);

    @Query("select coalesce(sum(s.total), 0) from Sale s where s.saleDate = :date")
    java.math.BigDecimal totalAmountByDate(LocalDate date);

    @Query("select coalesce(sum(s.quantity), 0) from Sale s where s.saleDate = :date and s.sellerName = :sellerName")
    Long totalUnitsByDateAndSeller(LocalDate date, String sellerName);

    @Query("select coalesce(sum(s.total), 0) from Sale s where s.saleDate = :date and s.sellerName = :sellerName")
    java.math.BigDecimal totalAmountByDateAndSeller(LocalDate date, String sellerName);

    @Query("select coalesce(sum(s.quantity), 0) from Sale s where s.sellerName = :sellerName")
    Long totalUnitsBySeller(String sellerName);

    @Query("select coalesce(sum(s.total), 0) from Sale s where s.sellerName = :sellerName")
    java.math.BigDecimal totalAmountBySeller(String sellerName);

    @Query("select new com.deli.dto.SellerDailySummary(s.sellerName, sum(s.quantity), sum(s.total)) from Sale s where s.saleDate = :date group by s.sellerName order by sum(s.total) desc")
    List<SellerDailySummary> dailySummaryBySeller(LocalDate date);

    @Query("select s from Sale s where s.sellerName = :sellerName order by s.createdAt desc, s.id desc")
    List<Sale> findBySellerNameOrderByCreatedAtDesc(String sellerName);
}