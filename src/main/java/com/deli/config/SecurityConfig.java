package com.deli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/error").permitAll()
                        .requestMatchers("/api/auth/me", "/api/product/current", "/api/sales").authenticated()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .httpBasic(basic -> { });
        return http.build();
    }

    @Bean
    UserDetailsService users(
            @Value("${APP_ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${APP_ADMIN_PASSWORD:cambia-admin-123}") String adminPassword,
            @Value("${APP_SELLER_USERNAME:vendedor}") String sellerUsername,
            @Value("${APP_SELLER_PASSWORD:cambia-vendedor-123}") String sellerPassword,
            PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        UserDetails seller = User.withUsername(sellerUsername)
                .password(passwordEncoder.encode(sellerPassword))
                .roles("SELLER")
                .build();
        return new InMemoryUserDetailsManager(admin, seller);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}