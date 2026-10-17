
package com.spendwise.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BudgetRequest(
        @NotBlank String category,
        @NotNull @DecimalMin("0.01") BigDecimal monthlyLimit,
        @NotNull LocalDate monthStart
) {}
