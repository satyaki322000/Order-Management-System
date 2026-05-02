package com.example.ordermanagementsystem.service;

import com.example.ordermanagementsystem.dto.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(OrderEvent event) {

        String key = event.getProductName(); // 🔥 helps partitioning

        kafkaTemplate.send("order-topic", key, event)
                .whenComplete((result, ex) -> {

                    if (ex == null) {
                        System.out.println("✅ Event sent successfully: " + event);
                    } else {
                        System.out.println("❌ Failed to send event: " + ex.getMessage());
                    }
                });
    }
}