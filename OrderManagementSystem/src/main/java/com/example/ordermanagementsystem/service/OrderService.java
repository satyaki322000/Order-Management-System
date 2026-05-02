package com.example.ordermanagementsystem.service;

import com.example.ordermanagementsystem.dto.ErrorResponse;
import com.example.ordermanagementsystem.dto.OrderEvent;
import com.example.ordermanagementsystem.entity.Order;
import com.example.ordermanagementsystem.exception.InventoryServiceException;
import com.example.ordermanagementsystem.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducerService producer;

    public OrderService(OrderRepository orderRepository,
                        KafkaProducerService producer) {
        this.orderRepository = orderRepository;
        this.producer = producer;
    }

    // 🔥 CREATE ORDER
    @Transactional
    public Order createOrder(Order order) {

        order.setStatus("CREATED");
        Order savedOrder = orderRepository.save(order);

        // 🔥 SEND EVENT
        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                "CREATE",
                order.getProductName(),
                order.getQuantity(),
                0
        );

        producer.sendEvent(event);

        return savedOrder;
    }

    // 🔥 GET ALL
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 🔥 UPDATE ORDER
    @Transactional
    public Order updateOrder(Long id, Order updatedOrder) {

        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        int oldQty = existing.getQuantity();
        int newQty = updatedOrder.getQuantity();

        existing.setQuantity(newQty);
        existing.setPrice(updatedOrder.getPrice());

        Order savedOrder = orderRepository.save(existing);

        // 🔥 SEND EVENT
        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                "UPDATE",
                existing.getProductName(),
                newQty,
                oldQty
        );

        producer.sendEvent(event);

        return savedOrder;
    }

    // 🔥 DELETE ORDER
    @Transactional
    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔥 SEND EVENT BEFORE DELETE
        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                "DELETE",
                order.getProductName(),
                order.getQuantity(),
                0
        );

        producer.sendEvent(event);

        orderRepository.deleteById(id);
    }
}
