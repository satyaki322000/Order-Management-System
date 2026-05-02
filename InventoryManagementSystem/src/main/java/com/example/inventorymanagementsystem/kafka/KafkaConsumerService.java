package com.example.inventorymanagementsystem.kafka;

import com.example.inventorymanagementsystem.dto.OrderEvent;
import com.example.inventorymanagementsystem.entity.ProcessedEvent;
import com.example.inventorymanagementsystem.repository.ProcessedEventRepository;
import com.example.inventorymanagementsystem.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final InventoryService inventoryService;

    public KafkaConsumerService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @Autowired
    private ProcessedEventRepository processedRepo;

    @KafkaListener(topics = "order-topic", groupId = "inventory-group")
    public void consume(OrderEvent event) {

        // 🔥 STEP 1: Check duplicate
        if (processedRepo.existsById(event.getEventId())) {
            System.out.println("Duplicate event skipped: " + event.getEventId());
            return;
        }

        // 🔥 STEP 2: Process event
        switch (event.getEventType()) {

            case "CREATE":
                inventoryService.decrease(event.getProductName(), event.getQuantity());
                break;

            case "DELETE":
                inventoryService.increase(event.getProductName(), event.getQuantity());
                break;

            case "UPDATE":
                int diff = event.getQuantity() - event.getOldQuantity();

                if (diff > 0) {
                    inventoryService.decrease(event.getProductName(), diff);
                } else if (diff < 0) {
                    inventoryService.increase(event.getProductName(), Math.abs(diff));
                }
                break;
        }

        // 🔥 STEP 3: Mark as processed
        System.out.println("Processing event: " + event);
        processedRepo.save(new ProcessedEvent(event.getEventId()));
    }
}