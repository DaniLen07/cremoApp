package com.deli.dto;

import com.deli.model.PaymentMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaleRequest(
                @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity,
                @NotNull(message = "Selecciona un medio de pago") PaymentMethod paymentMethod,
                @NotBlank(message = "Ingresa el nombre del vendedor") String sellerName,
                @Min(value = 0, message = "La cantidad de arequipe no puede ser negativa") int arequipe,
                @Min(value = 0, message = "La cantidad de leche en polvo no puede ser negativa") int powderedMilk,
                @Min(value = 0, message = "La cantidad de pasas no puede ser negativa") int raisins) {

        public SaleRequest(int quantity, PaymentMethod paymentMethod, String sellerName) {
                this(quantity, paymentMethod, sellerName, 0, 0, 0);
        }
}