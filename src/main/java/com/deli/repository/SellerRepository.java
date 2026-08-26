package com.deli.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deli.model.Seller;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByUsernameAndActiveTrue(String username);

    List<Seller> findByActiveTrueOrderByNameAsc();

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    boolean existsByNameIgnoreCase(String name);
}