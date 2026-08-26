package com.deli.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record PriceRequest(
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero") BigDecimal price) {
}