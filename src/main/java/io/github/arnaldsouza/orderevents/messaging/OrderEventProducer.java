package io.github.arnaldsouza.orderevents.messaging;

import io.github.arnaldsouza.orderevents.config.KafkaTopicConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.orderId());
        kafkaTemplate.send(KafkaTopicConfig.ORDERS_CREATED_TOPIC, key, event);
    }
}