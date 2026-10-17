
package com.spendwise.spendwise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SavingsGoalRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull @DecimalMin("0.01") BigDecimal targetAmount,
        @NotNull @DecimalMin("0.00") BigDecimal currentAmount,
        LocalDate deadline,
        @Size(max = 1000) String description
) {}
