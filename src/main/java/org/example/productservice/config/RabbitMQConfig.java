package org.example.productservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "product.exchange";

    // Routing keys
    public static final String PRODUCT_CREATED_KEY = "product.created";
    public static final String PRODUCT_UPDATED_KEY = "product.updated";
    public static final String PRODUCT_DELETED_KEY = "product.deleted";

    // Queues
    public static final String INVENTORY_QUEUE    = "inventory.product.events";
    public static final String NOTIFICATION_QUEUE = "notification.product.events";

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    // inventory-service recibe todos los eventos de producto
    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(inventoryQueue).to(productExchange).with("product.*");
    }

    // notification-service solo recibe creaciones
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(notificationQueue).to(productExchange).with(PRODUCT_CREATED_KEY);
    }

    // Serializa los mensajes a JSON
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
