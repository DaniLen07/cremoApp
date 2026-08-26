package com.deli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerRequest(
        @NotBlank(message = "Ingresa el nombre del vendedor") @Size(max = 120) String name,
        @NotBlank(message = "Ingresa el telefono del vendedor") @Size(max = 30) String phone,
        @NotBlank(message = "Ingresa el usuario") @Size(min = 3, max = 60) String username,
        @NotBlank(message = "Ingresa la contraseña") @Size(min = 8, max = 100) String password) {
}