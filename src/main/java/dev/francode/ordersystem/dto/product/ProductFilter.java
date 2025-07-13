package dev.francode.ordersystem.dto.product;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductFilter {

    @Size(max = 20, message = "El nombre no puede tener más de 20 caracteres.")
    private String name;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio mínimo no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El precio mínimo debe tener hasta 2 decimales.")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio máximo no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El precio máximo debe tener hasta 2 decimales.")
    private BigDecimal maxPrice;

    private Long categoryId;

    private Boolean status;

    // Validación minPrice no puede ser mayor que maxPrice
    public boolean isPriceRangeValid() {
        return (minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0);
    }
}
