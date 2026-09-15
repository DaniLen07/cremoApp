package com.deli.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
    private int arequipeQuantity;
    private int powderedMilkQuantity;
    private int raisinsQuantity;

    protected DailyInventory() {
    }

    public DailyInventory(Product product, LocalDate inventoryDate, int quantity) {
        this(product, inventoryDate, quantity, 0, 0, 0);
    }

    public DailyInventory(Product product, LocalDate inventoryDate, int quantity, int arequipeQuantity,
            int powderedMilkQuantity, int raisinsQuantity) {
        this.product = product;
        this.inventoryDate = inventoryDate;
        this.availableQuantity = quantity;
        this.initialQuantity = quantity;
        this.arequipeQuantity = arequipeQuantity;
        this.powderedMilkQuantity = powderedMilkQuantity;
        this.raisinsQuantity = raisinsQuantity;
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

    public int getArequipeQuantity() {
        return arequipeQuantity;
    }

    public int getPowderedMilkQuantity() {
        return powderedMilkQuantity;
    }

    public int getRaisinsQuantity() {
        return raisinsQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public void setInitialQuantity(int initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public void setArequipeQuantity(int arequipeQuantity) {
        this.arequipeQuantity = arequipeQuantity;
    }

    public void setPowderedMilkQuantity(int powderedMilkQuantity) {
        this.powderedMilkQuantity = powderedMilkQuantity;
    }

    public void setRaisinsQuantity(int raisinsQuantity) {
        this.raisinsQuantity = raisinsQuantity;
    }
}