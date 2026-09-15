package com.deli.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deli.model.SaleAudit;

public interface SaleAuditRepository extends JpaRepository<SaleAudit, Long> {
}
