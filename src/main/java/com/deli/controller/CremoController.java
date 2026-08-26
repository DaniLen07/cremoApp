package com.deli.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deli.dto.InventoryRequest;
import com.deli.dto.LoginRequest;
import com.deli.dto.PriceRequest;
import com.deli.dto.SaleRequest;
import com.deli.dto.SellerRequest;
import com.deli.model.DailyInventory;
import com.deli.model.Product;
import com.deli.model.Sale;
import com.deli.model.Seller;
import com.deli.service.CremoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CremoController {
    private final CremoService service;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public CremoController(CremoService service, AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return currentUser(authentication);
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/auth/me")
    public Map<String, Object> currentUser(Authentication authentication) {
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("username", authentication.getName());
        user.put("role", authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .findFirst().orElse("SELLER"));
        return user;
    }

    @GetMapping("/product/current")
    public Product currentProduct() {
        return service.getProduct();
    }

    @GetMapping("/sellers")
    public java.util.List<Seller> sellers() {
        return service.getSellers();
    }

    @PostMapping("/sellers")
    @ResponseStatus(HttpStatus.CREATED)
    public Seller registerSeller(@Valid @RequestBody SellerRequest request) {
        return service.registerSeller(request);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }

    @GetMapping("/inventory/today")
    public DailyInventory inventory() {
        return service.getTodayInventory();
    }

    @PutMapping("/inventory/today")
    public DailyInventory updateInventory(@Valid @RequestBody InventoryRequest request) {
        return service.updateInventory(request);
    }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public Sale createSale(@Valid @RequestBody SaleRequest request) {
        return service.createSale(request);
    }

    @GetMapping("/reports/weekly")
    public Map<String, Object> weeklyReport() {
        return service.weeklyReport();
    }

    @PutMapping("/product/price")
    public Product updatePrice(@Valid @RequestBody PriceRequest request) {
        return service.updatePrice(request);
    }
}