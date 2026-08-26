package com.deli.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "daily_inventory", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id",
        "inventory_date" }))
public class DailyInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private LocalDate inventoryDate;
    private int availableQuantity;
    private int initialQuantity;

    protected DailyInventory() {
    }

    public DailyInventory(Product product, LocalDate inventoryDate, int quantity) {
        this.product = product;
        this.inventoryDate = inventoryDate;
        this.availableQuantity = quantity;
        this.initialQuantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDate getInventoryDate() {
        return inventoryDate;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getInitialQuantity() {
        return initialQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }
}