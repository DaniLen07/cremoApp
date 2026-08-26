package com.deli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.deli.dto.SaleRequest;
import com.deli.model.PaymentMethod;
import org.junit.jupiter.api.Test;

class SaleSellerTest {

    @Test
    void saleRequestIncludesSellerName() {
        SaleRequest request = new SaleRequest(3, PaymentMethod.EFECTIVO, "Daniel");

        assertEquals("Daniel", request.sellerName());
    }
}
