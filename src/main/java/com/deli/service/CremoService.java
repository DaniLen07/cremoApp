package com.deli.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.deli.dto.InventoryRequest;
import com.deli.dto.PriceRequest;
import com.deli.dto.SaleRequest;
import com.deli.dto.SellerRequest;
import com.deli.model.DailyInventory;
import com.deli.model.Product;
import com.deli.model.Sale;
import com.deli.model.Seller;
import com.deli.repository.InventoryRepository;
import com.deli.repository.ProductRepository;
import com.deli.repository.SaleRepository;
import com.deli.repository.SellerRepository;

@Service
public class CremoService {
    private static final ZoneId COLOMBIA = ZoneId.of("America/Bogota");
    private static final Set<String> SELLER_NAMES = Set.of(
            "Juan Diego", "Christopher", "Salomé", "Daniel", "Luisa", "Otro");

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final SaleRepository saleRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String fallbackSellerUsername;

    public CremoService(ProductRepository productRepository, InventoryRepository inventoryRepository,
            SaleRepository saleRepository, SellerRepository sellerRepository, PasswordEncoder passwordEncoder,
            @Value("${APP_ADMIN_USERNAME:juandiego123}") String adminUsername,
            @Value("${APP_SELLER_USERNAME:vendedor}") String fallbackSellerUsername) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.saleRepository = saleRepository;
        this.sellerRepository = sellerRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.fallbackSellerUsername = fallbackSellerUsername;
    }

    public Product getProduct() {
        return productRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay producto configurado"));
    }

    public DailyInventory getTodayInventory() {
        Product product = getProduct();
        LocalDate today = LocalDate.now(COLOMBIA);
        return inventoryRepository.findByProductIdAndInventoryDate(product.getId(), today)
                .orElseGet(() -> inventoryRepository.save(new DailyInventory(product, today, 0)));
    }

    @Transactional
    public DailyInventory updateInventory(InventoryRequest request) {
        Product product = getProduct();
        LocalDate today = LocalDate.now(COLOMBIA);
        DailyInventory inventory = inventoryRepository.findByProductIdAndInventoryDate(product.getId(), today)
                .orElseGet(() -> new DailyInventory(product, today, request.quantity()));
        inventory.setInitialQuantity(request.quantity());
        inventory.setAvailableQuantity(request.quantity());
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Sale createSale(SaleRequest request) {
        if (request.paymentMethod() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un medio de pago");
        }

        String sellerName = request.sellerName() == null ? "No especificado" : request.sellerName().trim();
        if (!SELLER_NAMES.contains(sellerName) && !sellerRepository.existsByNameIgnoreCase(sellerName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un vendedor válido");
        }

        Product product = getProduct();
        DailyInventory inventory = getTodayInventory();
        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay inventario suficiente para esta venta");
        }
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.quantity());
        inventoryRepository.save(inventory);
        return saleRepository.save(new Sale(product, request.quantity(), request.paymentMethod(), sellerName));
    }

    public Map<String, Object> dashboard() {
        LocalDate today = LocalDate.now(COLOMBIA);
        DailyInventory inventory = getTodayInventory();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("product", getProduct());
        result.put("inventory", inventory);
        result.put("todayUnits", saleRepository.totalUnitsByDate(today));
        result.put("todayTotal", saleRepository.totalAmountByDate(today));
        result.put("recentSales", saleRepository.findTop20ByOrderByCreatedAtDesc());
        return result;
    }

    public List<Sale> weeklySales() {
        LocalDate end = LocalDate.now(COLOMBIA);
        LocalDate start = end.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return saleRepository.findBySaleDateBetweenOrderByCreatedAtDesc(start, end);
    }

    public Map<String, Object> weeklyReport() {
        List<Sale> sales = weeklySales();
        BigDecimal total = sales.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int units = sales.stream().mapToInt(Sale::getQuantity).sum();
        Map<String, Object> report = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(COLOMBIA);
        report.put("start", today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        report.put("end", today);
        report.put("units", units);
        report.put("total", total);
        report.put("sales", sales);
        return report;
    }

    public Product updatePrice(PriceRequest request) {
        Product product = getProduct();
        product.setPrice(request.price());
        return productRepository.save(product);
    }

    public List<Seller> getSellers() {
        return sellerRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional
    public Seller registerSeller(SellerRequest request) {
        String username = request.username().trim();
        String name = request.name().trim();
        if (username.equalsIgnoreCase(adminUsername) || username.equalsIgnoreCase(fallbackSellerUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario esta reservado");
        }
        if (sellerRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya esta registrado");
        }
        if (sellerRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese vendedor ya esta registrado");
        }
        return sellerRepository.save(new Seller(name, request.phone().trim(), username,
                passwordEncoder.encode(request.password())));
    }

    @Transactional
    public void resetSalesData() {
        saleRepository.deleteAllInBatch();
    }
}