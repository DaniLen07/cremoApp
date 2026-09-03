package com.deli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/api/auth/login"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/login.html", "/seller.html", "/app.js", "/login.js",
                                "/seller.js", "/styles.css", "/error")
                        .permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/csrf", "/api/health")
                        .permitAll()
                        .requestMatchers("/api/auth/me", "/api/product/current", "/api/sales",
                                "/api/seller/me/stats")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/inventory/today").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/sellers").authenticated()
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll())
                .securityContext(context -> context.securityContextRepository(securityContextRepository()));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}