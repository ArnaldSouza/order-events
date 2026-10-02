package io.github.arnaldsouza.orderevents.order;

import io.github.arnaldsouza.orderevents.order.dto.CreateOrderRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(request.customer(), request.amount());
        return orderRepository.save(order);
    }
}