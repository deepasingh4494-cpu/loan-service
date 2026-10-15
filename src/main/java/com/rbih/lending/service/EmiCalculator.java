package com.rbih.lending.service;

import java.math.BigDecimal;

public interface EmiCalculator {
    BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths);
}
