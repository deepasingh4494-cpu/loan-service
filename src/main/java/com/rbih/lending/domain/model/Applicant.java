package com.rbih.lending.domain.model;

import com.rbih.lending.domain.enums.EmploymentType;
import java.math.BigDecimal;

public record Applicant(String name, int age, BigDecimal monthlyIncome,
                        EmploymentType employmentType, int creditScore) {
}
