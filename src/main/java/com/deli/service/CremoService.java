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
import com.deli.dto.SellerDailySummary;
import com.deli.dto.SellerRequest;
import com.deli.dto.SellerUpdateRequest;
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
            @Value("${APP_ADMIN_USERNAME:juanm20}") String adminUsername,
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
        return createSale(request, null, true);
    }

    @Transactional
    public Sale createSale(SaleRequest request, String username, boolean admin) {
        if (request.paymentMethod() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un medio de pago");
        }

        String sellerName = request.sellerName() == null ? "No especificado" : request.sellerName().trim();
        if (!admin && username != null) {
            sellerName = sellerRepository.findByUsernameAndActiveTrue(username)
                    .map(Seller::getName)
                    .orElse(sellerName);
        }
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

    public Map<String, Object> sellerStats(String username) {
        Seller seller = sellerRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
        LocalDate today = LocalDate.now(COLOMBIA);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("seller", seller);
        stats.put("today", today);
        stats.put("todayUnits", saleRepository.totalUnitsByDateAndSeller(today, seller.getName()));
        stats.put("todayTotal", saleRepository.totalAmountByDateAndSeller(today, seller.getName()));
        stats.put("totalUnits", saleRepository.totalUnitsBySeller(seller.getName()));
        stats.put("totalAmount", saleRepository.totalAmountBySeller(seller.getName()));
        stats.put("sales", saleRepository.findBySellerNameOrderByCreatedAtDesc(seller.getName()));
        return stats;
    }

    public List<SellerDailySummary> dailySellerStats() {
        return saleRepository.dailySummaryBySeller(LocalDate.now(COLOMBIA));
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
    public Seller updateSeller(Long id, SellerUpdateRequest request) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
        String username = request.username().trim();
        String name = request.name().trim();
        if (username.equalsIgnoreCase(adminUsername) || username.equalsIgnoreCase(fallbackSellerUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario esta reservado");
        }
        if (sellerRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya esta registrado");
        }
        if (sellerRepository.existsByNameIgnoreCase(name) && !seller.getName().equalsIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese vendedor ya esta registrado");
        }
        seller.update(name, request.phone().trim(), username);
        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La contraseña debe tener al menos 8 caracteres");
            }
            seller.updatePassword(passwordEncoder.encode(request.password()));
        }
        return sellerRepository.save(seller);
    }

    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado"));
        seller.deactivate();
        sellerRepository.save(seller);
    }

    @Transactional
    public void resetSalesData() {
        saleRepository.deleteAllInBatch();
    }
}