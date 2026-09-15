package com.deli.dto;

import jakarta.validation.constraints.Min;

public record InventoryRequest(
        @Min(value = 0, message = "La cantidad no puede ser negativa") int quantity,
        @Min(value = 0, message = "La cantidad de arequipe no puede ser negativa") int arequipeQuantity,
        @Min(value = 0, message = "La cantidad de leche en polvo no puede ser negativa") int powderedMilkQuantity,
        @Min(value = 0, message = "La cantidad de pasas no puede ser negativa") int raisinsQuantity) {

    public InventoryRequest(int quantity) {
        this(quantity, 0, 0, 0);
    }
}