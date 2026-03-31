package org.example.productservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.config.RabbitMQConfig;
import org.example.productservice.dto.ProductResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishCreated(ProductResponse product) {
        publish(buildEvent("CREATED", product), RabbitMQConfig.PRODUCT_CREATED_KEY);
    }

    public void publishUpdated(ProductResponse product) {
        publish(buildEvent("UPDATED", product), RabbitMQConfig.PRODUCT_UPDATED_KEY);
    }

    public void publishDeleted(ProductResponse product) {
        publish(buildEvent("DELETED", product), RabbitMQConfig.PRODUCT_DELETED_KEY);
    }

    private void publish(ProductEvent event, String routingKey) {
        log.info("Publishing event [{}] for product {}", event.getEventType(), event.getProductId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }

    private ProductEvent buildEvent(String eventType, ProductResponse product) {
        return ProductEvent.builder()
                .eventType(eventType)
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .brand(product.getBrand())
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
