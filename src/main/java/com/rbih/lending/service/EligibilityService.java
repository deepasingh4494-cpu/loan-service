package com.rbih.lending.service;

import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.EligibilityResult;
import com.rbih.lending.domain.model.Loan;
import java.math.BigDecimal;

public interface EligibilityService {
    EligibilityResult evaluate(Applicant applicant, Loan loan, BigDecimal emi);
}
