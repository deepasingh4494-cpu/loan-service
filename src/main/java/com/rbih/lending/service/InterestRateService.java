package com.rbih.lending.service;

import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.RiskBand;
import java.math.BigDecimal;

public interface InterestRateService {
    BigDecimal calculate(RiskBand riskBand, EmploymentType employmentType, BigDecimal loanAmount);
}
