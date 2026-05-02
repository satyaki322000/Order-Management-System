package com.example.ordermanagementsystem.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String message;
    private String service;
    private int status;
}