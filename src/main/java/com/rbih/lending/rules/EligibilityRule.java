package com.rbih.lending.rules;

import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.Loan;
import java.math.BigDecimal;
import java.util.Optional;

public interface EligibilityRule {
    Optional<RejectionReason> evaluate(Applicant applicant, Loan loan, BigDecimal emi);
}
