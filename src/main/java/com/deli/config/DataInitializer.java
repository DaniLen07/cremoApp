package com.deli.config;

import com.deli.model.Product;
import com.deli.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedProduct(ProductRepository productRepository) {
        return args -> productRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseGet(() -> productRepository.save(new Product("Arroz con leche", new BigDecimal("5000"))));
    }
}