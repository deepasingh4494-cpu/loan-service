package com.rbih.lending.dto.request;

import com.rbih.lending.domain.enums.LoanPurpose;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LoanRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "10000", message = "amount must be between 10,000 and 50,00,000")
        @DecimalMax(value = "5000000", message = "amount must be between 10,000 and 50,00,000")
        BigDecimal amount,
        @Min(value = 6, message = "tenureMonths must be between 6 and 360")
        @Max(value = 360, message = "tenureMonths must be between 6 and 360") int tenureMonths,
        @NotNull(message = "purpose is required (PERSONAL, HOME or AUTO)") LoanPurpose purpose) {
}
