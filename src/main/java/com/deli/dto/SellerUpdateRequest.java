package com.deli.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellerUpdateRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Size(min = 3, max = 60) String username,
        @Size(max = 100) String password) {
}