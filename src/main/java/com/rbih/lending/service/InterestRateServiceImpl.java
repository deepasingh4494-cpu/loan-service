package com.rbih.lending.service;

import com.rbih.lending.config.LendingProperties;
import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.RiskBand;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class InterestRateServiceImpl implements InterestRateService {

    private static final BigDecimal LARGE_LOAN_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal LARGE_LOAN_PREMIUM = new BigDecimal("0.5");
    private static final BigDecimal SELF_EMPLOYED_PREMIUM = BigDecimal.ONE;

    private final LendingProperties properties;

    public InterestRateServiceImpl(LendingProperties properties) {
        this.properties = properties;
    }

    @Override
    public BigDecimal calculate(RiskBand riskBand, EmploymentType employmentType, BigDecimal loanAmount) {
        return properties.baseRatePercent()
                .add(riskPremium(riskBand))
                .add(employmentPremium(employmentType))
                .add(loanSizePremium(loanAmount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal riskPremium(RiskBand riskBand) {
        return switch (riskBand) {
            case LOW -> BigDecimal.ZERO;
            case MEDIUM -> new BigDecimal("1.5");
            case HIGH -> new BigDecimal("3");
        };
    }

    private BigDecimal employmentPremium(EmploymentType employmentType) {
        return switch (employmentType) {
            case SALARIED -> BigDecimal.ZERO;
            case SELF_EMPLOYED -> SELF_EMPLOYED_PREMIUM;
        };
    }

    private BigDecimal loanSizePremium(BigDecimal loanAmount) {
        return loanAmount.compareTo(LARGE_LOAN_THRESHOLD) > 0 ? LARGE_LOAN_PREMIUM : BigDecimal.ZERO;
    }
}
