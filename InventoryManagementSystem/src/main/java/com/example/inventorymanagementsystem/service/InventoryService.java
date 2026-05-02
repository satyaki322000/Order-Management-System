package com.example.inventorymanagementsystem.service;

import com.example.inventorymanagementsystem.entity.Inventory;
import com.example.inventorymanagementsystem.exception.ProductNotFoundException;
import com.example.inventorymanagementsystem.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public boolean checkAndReduce(String product, int qty) {

        Inventory inventory = repository.findByProductName(product)
                .orElseThrow(() ->
                        new ProductNotFoundException("Product not found: " + product)
                );

        if (inventory.getQuantity() < qty) {
            return false; // business case, not exception
        }

        inventory.setQuantity(inventory.getQuantity() - qty);
        repository.save(inventory);

        return true;
    }
    public boolean increase(String product, int qty) {

        Inventory inventory = repository.findByProductName(product)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + product));

        inventory.setQuantity(inventory.getQuantity() + qty);
        repository.save(inventory);

        return true;
    }

    public boolean decrease(String product, int qty) {

        Inventory inventory = repository.findByProductName(product)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + product));

        if (inventory.getQuantity() < qty) return false;

        inventory.setQuantity(inventory.getQuantity() - qty);
        repository.save(inventory);

        return true;
    }
}