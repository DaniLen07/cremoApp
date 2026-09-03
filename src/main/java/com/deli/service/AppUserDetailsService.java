package com.deli.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.deli.model.Seller;
import com.deli.repository.SellerRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String fallbackSellerUsername;
    private final String fallbackSellerPassword;

    public AppUserDetailsService(
            SellerRepository sellerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${APP_ADMIN_USERNAME}") String adminUsername,
            @Value("${APP_ADMIN_PASSWORD}") String adminPassword,
            @Value("${APP_SELLER_USERNAME}") String fallbackSellerUsername,
            @Value("${APP_SELLER_PASSWORD}") String fallbackSellerPassword) {
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.fallbackSellerUsername = fallbackSellerUsername;
        this.fallbackSellerPassword = fallbackSellerPassword;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (adminUsername.equalsIgnoreCase(username)) {
            return User.withUsername(adminUsername).password(passwordEncoder.encode(adminPassword)).roles("ADMIN")
                    .build();
        }
        if (fallbackSellerUsername.equalsIgnoreCase(username)) {
            return User.withUsername(fallbackSellerUsername).password(passwordEncoder.encode(fallbackSellerPassword))
                    .roles("SELLER").build();
        }
        Seller seller = sellerRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(seller.getUsername()).password(seller.getPasswordHash()).roles("SELLER").build();
    }
}