package com.deli.dto;

import com.deli.model.PaymentMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaleRequest(
                @Min(value = 1, message = "La cantidad debe ser mayor que cero") int quantity,
                @NotNull(message = "Selecciona un medio de pago") PaymentMethod paymentMethod,
                @NotBlank(message = "Ingresa el nombre del vendedor") String sellerName,
                boolean arequipe,
                boolean powderedMilk,
                boolean raisins) {

        public SaleRequest(int quantity, PaymentMethod paymentMethod, String sellerName) {
                this(quantity, paymentMethod, sellerName, false, false, false);
        }
}