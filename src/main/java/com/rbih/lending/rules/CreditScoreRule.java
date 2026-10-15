package com.rbih.lending.rules;

import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.Loan;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CreditScoreRule implements EligibilityRule {

    private static final int MINIMUM_CREDIT_SCORE = 600;

    @Override
    public Optional<RejectionReason> evaluate(Applicant applicant, Loan loan, BigDecimal emi) {
        return applicant.creditScore() < MINIMUM_CREDIT_SCORE
                ? Optional.of(RejectionReason.CREDIT_SCORE_TOO_LOW)
                : Optional.empty();
    }
}
