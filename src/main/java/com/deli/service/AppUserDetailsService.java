package com.deli.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.deli.model.Seller;
import com.deli.repository.SellerRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final Map<String, LoginAttempts> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 15 * 60 * 1000L;

    public AppUserDetailsService(
            SellerRepository sellerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${APP_ADMIN_USERNAME:juanm20}") String adminUsername,
            @Value("${APP_ADMIN_PASSWORD:jm6181}") String adminPassword) {
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String key = username == null ? "" : username.toLowerCase();
        LoginAttempts loginAttempts = attempts.computeIfAbsent(key, ignored -> new LoginAttempts());
        if (loginAttempts.locked()) {
            throw new UsernameNotFoundException("Cuenta temporalmente bloqueada. Intenta de nuevo más tarde.");
        }
        if (adminUsername.equalsIgnoreCase(username)) {
            return User.withUsername(adminUsername).password(passwordEncoder.encode(adminPassword)).roles("ADMIN")
                    .build();
        }
        Seller seller = sellerRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(seller.getUsername()).password(seller.getPasswordHash()).roles("SELLER").build();
    }

    public void resetAttempts(String username) {
        if (username != null)
            attempts.remove(username.toLowerCase());
    }

    public void registerFailure(String username) {
        if (username == null)
            return;
        LoginAttempts loginAttempts = attempts.computeIfAbsent(username.toLowerCase(), ignored -> new LoginAttempts());
        loginAttempts.failure();
    }

    private static final class LoginAttempts {
        private int failures;
        private long lockedUntil;

        private synchronized boolean locked() {
            long now = Instant.now().toEpochMilli();
            if (lockedUntil > now)
                return true;
            if (lockedUntil != 0) {
                failures = 0;
                lockedUntil = 0;
            }
            return false;
        }

        private synchronized void failure() {
            failures++;
            if (failures >= MAX_ATTEMPTS)
                lockedUntil = Instant.now().toEpochMilli() + LOCK_MILLIS;
        }
    }
}