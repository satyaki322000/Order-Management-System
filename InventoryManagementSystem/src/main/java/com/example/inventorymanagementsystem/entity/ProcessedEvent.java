package com.example.inventorymanagementsystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor   // 🔥 ADD THIS
public class ProcessedEvent {

    @Id
    private String eventId;
}