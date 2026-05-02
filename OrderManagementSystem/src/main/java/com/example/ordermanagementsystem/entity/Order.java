package com.example.ordermanagementsystem.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Product name is required")
    private String productName;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    @Min(value = 1, message = "Price must be greater than 0")
    private double price;
    private String status;

    @Version
    private Long version;

}
