package com.deli.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale_audit")
public class SaleAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long saleId;
    private String action;
    private String actorUsername;
    private String sellerName;
    private LocalDateTime occurredAt;

    protected SaleAudit() {
    }

    public SaleAudit(Long saleId, String action, String actorUsername, String sellerName, LocalDateTime occurredAt) {
        this.saleId = saleId;
        this.action = action;
        this.actorUsername = actorUsername;
        this.sellerName = sellerName;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSaleId() {
        return saleId;
    }

    public String getAction() {
        return action;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getSellerName() {
        return sellerName;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
