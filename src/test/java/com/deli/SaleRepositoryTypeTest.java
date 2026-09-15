package com.deli;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.deli.repository.SaleRepository;

class SaleRepositoryTypeTest {

    @Test
    void totalUnitsAggregateUsesLongResultType() throws NoSuchMethodException {
        Method method = SaleRepository.class.getDeclaredMethod("totalUnitsByDate", LocalDate.class);
        assertEquals(Long.class, method.getReturnType());
    }
}
