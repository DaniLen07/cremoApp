package com.deli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.deli.dto.SaleRequest;
import com.deli.model.PaymentMethod;
import com.deli.model.Product;
import com.deli.model.Sale;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SaleSellerTest {

    @Test
    void saleRequestIncludesSellerName() {
        SaleRequest request = new SaleRequest(3, PaymentMethod.EFECTIVO, "Daniel");

        assertEquals("Daniel", request.sellerName());
    }

    @Test
    void saleTotalIncludesSelectedToppingsPerUnit() {
        Sale sale = new Sale(new Product("Arroz con leche", new BigDecimal("5000")), 2,
                PaymentMethod.EFECTIVO, "Daniel", true, false, true);

        assertEquals(0, sale.getUnitPrice().compareTo(new BigDecimal("7000")));
        assertEquals(0, sale.getTotal().compareTo(new BigDecimal("14000")));
        assertEquals(0, sale.getToppingsTotal().compareTo(new BigDecimal("4000")));
    }
}
