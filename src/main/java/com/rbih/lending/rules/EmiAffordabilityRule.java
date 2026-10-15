package com.rbih.lending.rules;

import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.Loan;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class EmiAffordabilityRule implements EligibilityRule {

    private static final BigDecimal ELIGIBILITY_LIMIT = new BigDecimal("0.60");
    private static final BigDecimal OFFER_LIMIT = new BigDecimal("0.50");

    @Override
    public Optional<RejectionReason> evaluate(Applicant applicant, Loan loan, BigDecimal emi) {
        BigDecimal income = applicant.monthlyIncome();
        if (emi.compareTo(income.multiply(ELIGIBILITY_LIMIT)) > 0) {
            return Optional.of(RejectionReason.EMI_EXCEEDS_60_PERCENT);
        }
        if (emi.compareTo(income.multiply(OFFER_LIMIT)) > 0) {
            return Optional.of(RejectionReason.EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT);
        }
        return Optional.empty();
    }
}
