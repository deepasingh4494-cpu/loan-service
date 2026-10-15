package com.rbih.lending.service;

import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.EligibilityResult;
import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.rules.EligibilityRule;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private final List<EligibilityRule> rules;

    public EligibilityServiceImpl(List<EligibilityRule> rules) {
        this.rules = rules;
    }

    @Override
    public EligibilityResult evaluate(Applicant applicant, Loan loan, BigDecimal emi) {
        List<RejectionReason> reasons = rules.stream()
                .map(rule -> rule.evaluate(applicant, loan, emi))
                .flatMap(Optional::stream)
                .toList();
        return new EligibilityResult(reasons.isEmpty(), reasons);
    }
}
