package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbih.lending.config.LendingProperties;
import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.RiskBand;
import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InterestRateServiceTest {

    private final InterestRateService service =
            new InterestRateServiceImpl(new LendingProperties(new BigDecimal("12.0")));

    @ParameterizedTest
    @CsvSource({

            "LOW,    SALARIED,      500000,  12.00",
            "MEDIUM, SALARIED,      500000,  13.50",
            "HIGH,   SALARIED,      500000,  15.00",
            "LOW,    SELF_EMPLOYED, 500000,  13.00",
            "MEDIUM, SELF_EMPLOYED, 500000,  14.50",
            "LOW,    SALARIED,      1000000, 12.00",
            "LOW,    SALARIED,      1000001, 12.50",
            "HIGH,   SELF_EMPLOYED, 5000000, 16.50"
    })
    void sumsBaseRateAndAllPremiums(RiskBand risk, EmploymentType employment, String amount, String expected) {
        BigDecimal rate = service.calculate(risk, employment, new BigDecimal(amount));

        assertThat(rate).isEqualByComparingTo(expected);
        assertThat(rate.scale()).isEqualTo(2);
    }
}
