package com.deli.repository;

import com.deli.model.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findFirstByActiveTrueOrderByIdAsc();
}