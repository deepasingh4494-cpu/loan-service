package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.LoanPurpose;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.EligibilityResult;
import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.rules.AgeTenureRule;
import com.rbih.lending.rules.CreditScoreRule;
import com.rbih.lending.rules.EmiAffordabilityRule;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EligibilityServiceTest {

    private final EligibilityService service = new EligibilityServiceImpl(
            List.of(new CreditScoreRule(), new AgeTenureRule(), new EmiAffordabilityRule()));

    private static Applicant applicant(int age, String income, int score) {
        return new Applicant("Test", age, new BigDecimal(income), EmploymentType.SALARIED, score);
    }

    private static Loan loan(int tenureMonths) {
        return new Loan(new BigDecimal("500000"), tenureMonths, LoanPurpose.PERSONAL);
    }

    @Test
    void approvesWhenAllRulesPass() {
        EligibilityResult result = service.evaluate(applicant(30, "75000", 720), loan(36), new BigDecimal("16000"));

        assertThat(result.eligible()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"599, false", "600, true"})
    void creditScoreBoundary(int score, boolean eligible) {
        EligibilityResult result = service.evaluate(applicant(30, "75000", score), loan(36), new BigDecimal("1000"));

        assertThat(result.eligible()).isEqualTo(eligible);
        if (!eligible) {
            assertThat(result.reasons()).containsExactly(RejectionReason.CREDIT_SCORE_TOO_LOW);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "60, 60,  true",   // 60 + 5.0 = 65 -> allowed
            "60, 61,  false",  // 60 + 5.08 > 65
            "40, 300, true",   // 40 + 25 = 65
            "45, 300, false",  // 45 + 25 = 70
            "21, 360, true"    // 21 + 30 = 51
    })
    void ageTenureBoundary(int age, int tenureMonths, boolean eligible) {
        EligibilityResult result = service.evaluate(applicant(age, "75000", 720), loan(tenureMonths), new BigDecimal("1000"));

        assertThat(result.eligible()).isEqualTo(eligible);
        if (!eligible) {
            assertThat(result.reasons()).containsExactly(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);
        }
    }

    @Test
    void emiAtExactlyFiftyPercentIsAllowed() {
        EligibilityResult result = service.evaluate(applicant(30, "50000", 720), loan(36), new BigDecimal("25000.00"));

        assertThat(result.eligible()).isTrue();
    }

    @Test
    void emiBetweenFiftyAndSixtyPercentFailsOfferLimit() {
        EligibilityResult result = service.evaluate(applicant(30, "50000", 720), loan(36), new BigDecimal("25000.01"));

        assertThat(result.reasons()).containsExactly(RejectionReason.EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT);
    }

    @Test
    void emiAboveSixtyPercentFailsEligibility() {
        EligibilityResult result = service.evaluate(applicant(30, "50000", 720), loan(36), new BigDecimal("30000.01"));

        assertThat(result.reasons()).containsExactly(RejectionReason.EMI_EXCEEDS_60_PERCENT);
    }

    @Test
    void collectsAllFailingReasons() {
        EligibilityResult result = service.evaluate(applicant(60, "20000", 550), loan(120), new BigDecimal("15000"));

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).containsExactlyInAnyOrder(
                RejectionReason.CREDIT_SCORE_TOO_LOW,
                RejectionReason.AGE_TENURE_LIMIT_EXCEEDED,
                RejectionReason.EMI_EXCEEDS_60_PERCENT);
    }
}
