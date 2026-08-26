package com.deli.dto;

import jakarta.validation.constraints.Min;

public record InventoryRequest(@Min(value = 0, message = "La cantidad no puede ser negativa") int quantity) {
}