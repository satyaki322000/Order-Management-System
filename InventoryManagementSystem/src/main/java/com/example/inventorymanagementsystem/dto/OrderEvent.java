package com.example.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {

    private String eventId;
    private String eventType;
    private String productName;
    private int quantity;
    private int oldQuantity;
}