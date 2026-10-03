package io.github.arnaldsouza.orderevents.order;

import io.github.arnaldsouza.orderevents.messaging.OrderCreatedEvent;
import io.github.arnaldsouza.orderevents.messaging.OrderEventProducer;
import io.github.arnaldsouza.orderevents.order.dto.CreateOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,
                        OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(request.customer(), request.amount());
        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomer(),
                savedOrder.getAmount(),
                savedOrder.getCreatedAt()
        );
        orderEventProducer.publishOrderCreated(event);

        return savedOrder;
    }

    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Order not found: " + orderId));
        order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", orderId, order.getStatus());
    }
}