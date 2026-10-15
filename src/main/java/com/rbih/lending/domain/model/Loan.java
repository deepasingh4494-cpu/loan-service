package com.rbih.lending.domain.model;

import com.rbih.lending.domain.enums.LoanPurpose;
import java.math.BigDecimal;

public record Loan(BigDecimal amount, int tenureMonths, LoanPurpose purpose) {
}
