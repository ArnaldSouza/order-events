package io.github.arnaldsouza.orderevents.messaging;

import io.github.arnaldsouza.orderevents.config.KafkaConsumerConfig;
import io.github.arnaldsouza.orderevents.config.KafkaTopicConfig;
import io.github.arnaldsouza.orderevents.order.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusConsumer.class);

    private final OrderService orderService;

    public OrderStatusConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = KafkaTopicConfig.ORDERS_CREATED_TOPIC,
            groupId = KafkaConsumerConfig.ORDERS_GROUP
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.orderId());
        orderService.confirmPayment(event.orderId());
    }
}