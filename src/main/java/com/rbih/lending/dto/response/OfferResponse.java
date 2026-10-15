package com.rbih.lending.dto.response;

import java.math.BigDecimal;

public record OfferResponse(BigDecimal interestRate, int tenureMonths, BigDecimal emi, BigDecimal totalPayable) {
}
