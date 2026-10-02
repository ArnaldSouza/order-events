package io.github.arnaldsouza.orderevents.order;

import io.github.arnaldsouza.orderevents.order.dto.CreateOrderRequest;
import io.github.arnaldsouza.orderevents.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            UriComponentsBuilder uriBuilder) {
        Order order = orderService.createOrder(request);
        URI location = uriBuilder
                .path("/orders/{id}")
                .buildAndExpand(order.getId())
                .toUri();
        return ResponseEntity.created(location).body(OrderResponse.from(order));
    }
}