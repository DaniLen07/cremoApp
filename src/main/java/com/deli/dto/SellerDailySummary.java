package com.deli.dto;

import java.math.BigDecimal;

public record SellerDailySummary(String sellerName, Long units, BigDecimal total) {
}