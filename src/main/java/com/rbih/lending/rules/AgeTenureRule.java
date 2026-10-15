package com.rbih.lending.rules;

import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.Loan;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AgeTenureRule implements EligibilityRule {

    private static final int MAX_AGE_AT_MATURITY_MONTHS = 65 * 12;

    @Override
    public Optional<RejectionReason> evaluate(Applicant applicant, Loan loan, BigDecimal emi) {
        // Compared in months so that fractional tenure years (e.g. 18 months) are not truncated.
        int ageAtMaturityMonths = applicant.age() * 12 + loan.tenureMonths();
        return ageAtMaturityMonths > MAX_AGE_AT_MATURITY_MONTHS
                ? Optional.of(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED)
                : Optional.empty();
    }
}
