package com.deli.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deli.dto.InventoryRequest;
import com.deli.dto.LoginRequest;
import com.deli.dto.PriceRequest;
import com.deli.dto.SaleRequest;
import com.deli.dto.SellerDailySummary;
import com.deli.dto.SellerRequest;
import com.deli.dto.SellerUpdateRequest;
import com.deli.model.DailyInventory;
import com.deli.model.Product;
import com.deli.model.Sale;
import com.deli.model.Seller;
import com.deli.service.AppUserDetailsService;
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
    private final AppUserDetailsService userDetailsService;

    public CremoController(CremoService service, AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository, AppUserDetailsService userDetailsService) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            userDetailsService.registerFailure(request.username());
            throw exception;
        }
        userDetailsService.resetAttempts(request.username());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        return currentUser(authentication);
    }

    @GetMapping("/auth/csrf")
    public Map<String, String> csrfToken(CsrfToken token) {
        return Map.of("token", token.getToken());
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

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/seller/me/stats")
    public Map<String, Object> sellerStats(Authentication authentication) {
        return service.sellerStats(authentication.getName());
    }

    @GetMapping("/admin/seller-stats")
    public java.util.List<SellerDailySummary> dailySellerStats() {
        return service.dailySellerStats();
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

    @PutMapping("/sellers/{id}")
    public Seller updateSeller(@PathVariable Long id, @Valid @RequestBody SellerUpdateRequest request) {
        return service.updateSeller(id, request);
    }

    @DeleteMapping("/sellers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeller(@PathVariable Long id) {
        service.deleteSeller(id);
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
    public Sale createSale(@Valid @RequestBody SaleRequest request, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.createSale(request, authentication.getName(), admin);
    }

    @PutMapping("/sales/{id}")
    public Sale updateSale(@PathVariable Long id, @Valid @RequestBody SaleRequest request,
            Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.updateSale(id, request, authentication.getName(), admin);
    }

    @DeleteMapping("/sales/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSale(@PathVariable Long id, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        service.deleteSale(id, authentication.getName(), admin);
    }

    @GetMapping("/reports/weekly")
    public Map<String, Object> weeklyReport(@RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end, @RequestParam(required = false) String seller,
            @RequestParam(required = false) String payment) {
        var sales = service.filteredSales(start, end, seller, payment);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("sales", sales);
        report.put("units", sales.stream().mapToInt(Sale::getQuantity).sum());
        report.put("total",
                sales.stream().map(Sale::getTotal).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        report.put("start", start);
        report.put("end", end);
        return report;
    }

    @GetMapping(value = "/reports/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportSales(@RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end, @RequestParam(required = false) String seller,
            @RequestParam(required = false) String payment) {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body("\uFEFF" + service.salesCsv(start, end, seller, payment));
    }

    @GetMapping("/reports/daily")
    public Map<String, Object> dailyReport() {
        return service.dailyReport();
    }

    @PutMapping("/product/price")
    public Product updatePrice(@Valid @RequestBody PriceRequest request) {
        return service.updatePrice(request);
    }
}