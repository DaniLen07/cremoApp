package com.deli.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales")
public class Sale {
    public static final BigDecimal TOPPING_PRICE = new BigDecimal("500");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal total;
    private LocalDate saleDate;
    private LocalDateTime createdAt;
    private String sellerName;
    private int arequipe;
    private int powderedMilk;
    private int raisins;
    private BigDecimal toppingsTotal;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    protected Sale() {
    }

    public Sale(Product product, int quantity, PaymentMethod paymentMethod, String sellerName) {
        this(product, quantity, paymentMethod, sellerName, 0, 0, 0);
    }

    public Sale(Product product, int quantity, PaymentMethod paymentMethod, String sellerName,
            int arequipe, int powderedMilk, int raisins) {
        this.product = product;
        this.quantity = quantity;
        this.arequipe = Math.max(0, arequipe);
        this.powderedMilk = Math.max(0, powderedMilk);
        this.raisins = Math.max(0, raisins);
        int toppingCount = this.arequipe + this.powderedMilk + this.raisins;
        BigDecimal toppingUnitPrice = TOPPING_PRICE.multiply(BigDecimal.valueOf(toppingCount));
        this.unitPrice = product.getPrice().add(toppingUnitPrice);
        this.total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.toppingsTotal = toppingUnitPrice.multiply(BigDecimal.valueOf(quantity));
        ZoneId colombia = ZoneId.of("America/Bogota");
        this.saleDate = LocalDate.now(colombia);
        this.createdAt = LocalDateTime.now(colombia);
        this.paymentMethod = paymentMethod;
        this.sellerName = sellerName == null || sellerName.isBlank() ? "No especificado" : sellerName.trim();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getSellerName() {
        return sellerName;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public int getArequipe() {
        return arequipe;
    }

    public int getPowderedMilk() {
        return powderedMilk;
    }

    public int getRaisins() {
        return raisins;
    }

    public String getToppingsSummary() {
        java.util.List<String> toppings = new java.util.ArrayList<>();
        if (arequipe > 0) {
            toppings.add("Arequipe: " + arequipe);
        }
        if (powderedMilk > 0) {
            toppings.add("Leche en polvo: " + powderedMilk);
        }
        if (raisins > 0) {
            toppings.add("Uvas pasas: " + raisins);
        }
        return toppings.isEmpty() ? "Sin toppings" : String.join(" | ", toppings);
    }

    public BigDecimal getToppingsTotal() {
        return toppingsTotal;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName == null || sellerName.isBlank() ? "No especificado" : sellerName.trim();
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setArequipe(int arequipe) {
        this.arequipe = Math.max(0, arequipe);
    }

    public void setPowderedMilk(int powderedMilk) {
        this.powderedMilk = Math.max(0, powderedMilk);
    }

    public void setRaisins(int raisins) {
        this.raisins = Math.max(0, raisins);
    }

    public void setToppingsTotal(BigDecimal toppingsTotal) {
        this.toppingsTotal = toppingsTotal;
    }

    public void setIdForUpdate(Long id) {
        this.id = id;
    }
}