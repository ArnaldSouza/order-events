package io.github.arnaldsouza.orderevents.order;

import io.github.arnaldsouza.orderevents.messaging.OrderCreatedEvent;
import io.github.arnaldsouza.orderevents.messaging.OrderEventProducer;
import io.github.arnaldsouza.orderevents.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldSaveOrderAndPublishEvent() {
        CreateOrderRequest request = new CreateOrderRequest("Arnald", new BigDecimal("199.90"));

        Order savedOrder = new Order("Arnald", new BigDecimal("199.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.createOrder(request);

        assertThat(result.getCustomer()).isEqualTo("Arnald");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).publishOrderCreated(any(OrderCreatedEvent.class));
    }

    @Test
    void confirmPayment_shouldUpdateStatusToPaymentConfirmed() {
        Order order = new Order("Arnald", new BigDecimal("50.00"));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.confirmPayment(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
        verify(orderRepository).save(order);
    }

    @Test
    void confirmPayment_shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.confirmPayment(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order not found");
    }
}