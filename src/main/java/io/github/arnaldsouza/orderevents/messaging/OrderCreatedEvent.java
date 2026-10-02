package io.github.arnaldsouza.orderevents.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String customer,
        BigDecimal amount,
        Instant createdAt
) {
}