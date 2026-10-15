package com.rbih.lending.dto.request;

import com.rbih.lending.domain.enums.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ApplicantRequest(
        @NotBlank(message = "name must not be blank") String name,
        @Min(value = 21, message = "age must be between 21 and 60")
        @Max(value = 60, message = "age must be between 21 and 60") int age,
        @NotNull(message = "monthlyIncome is required")
        @DecimalMin(value = "0", inclusive = false, message = "monthlyIncome must be greater than 0")
        BigDecimal monthlyIncome,
        @NotNull(message = "employmentType is required (SALARIED or SELF_EMPLOYED)") EmploymentType employmentType,
        @Min(value = 300, message = "creditScore must be between 300 and 900")
        @Max(value = 900, message = "creditScore must be between 300 and 900") int creditScore) {
}
