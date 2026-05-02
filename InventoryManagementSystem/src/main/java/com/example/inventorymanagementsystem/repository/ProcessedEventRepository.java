package com.example.inventorymanagementsystem.repository;

import com.example.inventorymanagementsystem.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}