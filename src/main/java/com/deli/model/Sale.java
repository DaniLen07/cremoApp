package com.deli.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "sales")
public class Sale {
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

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    protected Sale() {
    }

    public Sale(Product product, int quantity, PaymentMethod paymentMethod, String sellerName) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
        this.total = unitPrice.multiply(BigDecimal.valueOf(quantity));
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
}