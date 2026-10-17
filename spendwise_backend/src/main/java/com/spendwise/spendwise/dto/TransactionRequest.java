package com.spendwise.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.spendwise.spendwise.entity.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionRequest(

    @NotBlank
    @Size(max = 100)
    String title,

    @NotNull
    @DecimalMin("0.01")
    BigDecimal amount,

    @NotNull
    TransactionType type,

    @NotBlank
    @Size(max = 50)
    String category,

    @NotNull
    LocalDate transactionDate,

    @Size(max = 500)
    String description

) {}