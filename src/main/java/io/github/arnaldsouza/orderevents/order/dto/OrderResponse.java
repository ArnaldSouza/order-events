package io.github.arnaldsouza.orderevents.order.dto;

import io.github.arnaldsouza.orderevents.order.Order;
import io.github.arnaldsouza.orderevents.order.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String customer,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer(),
                order.getAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}