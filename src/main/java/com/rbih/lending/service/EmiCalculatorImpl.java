package com.rbih.lending.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class EmiCalculatorImpl implements EmiCalculator {

    private static final int MONTHS_PER_YEAR = 12;
    private static final BigDecimal PERCENT = BigDecimal.valueOf(100);
    private static final int RESULT_SCALE = 2;
    // Intermediate precision is kept high so that (1+r)^n does not lose cents on long tenures.
    private static final MathContext CALC_PRECISION = new MathContext(20, RoundingMode.HALF_UP);

    @Override
    public BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be greater than zero");
        }
        if (annualRatePercent == null || annualRatePercent.signum() < 0) {
            throw new IllegalArgumentException("Annual rate must not be negative");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure must be greater than zero");
        }

        if (annualRatePercent.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), RESULT_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), CALC_PRECISION)
                .divide(PERCENT, CALC_PRECISION);
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths, CALC_PRECISION);

        return principal.multiply(monthlyRate, CALC_PRECISION)
                .multiply(growth, CALC_PRECISION)
                .divide(growth.subtract(BigDecimal.ONE), RESULT_SCALE, RoundingMode.HALF_UP);
    }
}
