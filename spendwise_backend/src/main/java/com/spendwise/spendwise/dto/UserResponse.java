package com.spendwise.spendwise.dto;
import java.time.LocalDateTime;
public record UserResponse(Long id, String name, String email, LocalDateTime createdAt) {}
