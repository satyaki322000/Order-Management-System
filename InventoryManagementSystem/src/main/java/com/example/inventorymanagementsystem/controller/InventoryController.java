package com.example.inventorymanagementsystem.controller;

import com.example.inventorymanagementsystem.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public boolean checkInventory(
            @RequestParam String product,
            @RequestParam int qty) {

        return service.checkAndReduce(product, qty);
    }
    @PostMapping("/increase")
    public boolean increaseStock(@RequestParam String product,
                                 @RequestParam int qty) {
        return service.increase(product, qty);
    }

    @PostMapping("/decrease")
    public boolean decreaseStock(@RequestParam String product,
                                 @RequestParam int qty) {
        return service.decrease(product, qty);
    }
}