package org.example.productservice.event;

import lombok.Builder;
import lombok.Data;
import org.example.productservice.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductEvent {

    private String eventType;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Product.Category category;
    private String brand;
    private LocalDateTime occurredAt;
}
