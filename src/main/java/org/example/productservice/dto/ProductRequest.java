package org.example.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.example.productservice.model.Product;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "La categoría es obligatoria")
    private Product.Category category;

    private String size;

    private String color;

    private String brand;

    private String imageUrl;
}
