package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmiCalculatorTest {

    private final EmiCalculator calculator = new EmiCalculatorImpl();

    @ParameterizedTest
    @CsvSource({
            "500000, 13.5, 36, 16967.64",
            "500000, 12,   36, 16607.15",
            "100000, 12,   12, 8884.88",
            "1200000, 15,  60, 28547.92",
            "10000,  12,   6,  1725.48"
    })
    void calculatesEmiRoundedHalfUpToTwoDecimals(String principal, String rate, int months, String expected) {
        BigDecimal emi = calculator.calculate(new BigDecimal(principal), new BigDecimal(rate), months);

        assertThat(emi).isEqualByComparingTo(expected);
        assertThat(emi.scale()).isEqualTo(2);
    }

    @Test
    void zeroInterestSplitsPrincipalEvenly() {
        BigDecimal emi = calculator.calculate(new BigDecimal("120000"), BigDecimal.ZERO, 12);

        assertThat(emi).isEqualByComparingTo("10000.00");
    }

    @Test
    void handlesMaximumTenureWithoutOverflowOrPrecisionLoss() {
        BigDecimal emi = calculator.calculate(new BigDecimal("5000000"), new BigDecimal("16.5"), 360);

        assertThat(emi.scale()).isEqualTo(2);
        assertThat(emi).isGreaterThan(new BigDecimal("5000000").multiply(new BigDecimal("0.165")).divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP));
    }

    @Test
    void rejectsNonPositivePrincipal() {
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ZERO, new BigDecimal("12"), 12))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeRate() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("1000"), new BigDecimal("-1"), 12))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveTenure() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("1000"), new BigDecimal("12"), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
