package com.rbih.lending.domain.model;

import java.math.BigDecimal;

public record Offer(BigDecimal interestRate, int tenureMonths, BigDecimal emi, BigDecimal totalPayable) {
}
