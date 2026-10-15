package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.EmploymentType;
import com.rbih.lending.domain.enums.LoanPurpose;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.enums.RiskBand;
import com.rbih.lending.domain.model.EligibilityResult;
import com.rbih.lending.domain.model.Offer;
import com.rbih.lending.dto.request.ApplicantRequest;
import com.rbih.lending.dto.request.LoanApplicationRequest;
import com.rbih.lending.dto.request.LoanRequest;
import com.rbih.lending.dto.response.LoanApplicationResponse;
import com.rbih.lending.entity.LoanApplicationEntity;
import com.rbih.lending.mapper.LoanApplicationMapper;
import com.rbih.lending.repository.LoanApplicationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock RiskClassificationService riskService;
    @Mock InterestRateService interestRateService;
    @Mock EmiCalculator emiCalculator;
    @Mock EligibilityService eligibilityService;
    @Mock OfferService offerService;
    @Mock LoanApplicationRepository repository;

    private LoanApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanApplicationServiceImpl(riskService, interestRateService, emiCalculator,
                eligibilityService, offerService, repository, new LoanApplicationMapper());
    }

    private static LoanApplicationRequest request(int score) {
        return new LoanApplicationRequest(
                new ApplicantRequest("Asha", 30, new BigDecimal("75000"), EmploymentType.SALARIED, score),
                new LoanRequest(new BigDecimal("500000"), 36, LoanPurpose.PERSONAL));
    }

    @Test
    void approvesEligibleApplicationAndStoresIt() {
        Offer offer = new Offer(new BigDecimal("13.50"), 36, new BigDecimal("16967.64"), new BigDecimal("610835.04"));
        when(riskService.classify(720)).thenReturn(Optional.of(RiskBand.MEDIUM));
        when(interestRateService.calculate(eq(RiskBand.MEDIUM), eq(EmploymentType.SALARIED), any()))
                .thenReturn(new BigDecimal("13.50"));
        when(emiCalculator.calculate(any(), eq(new BigDecimal("13.50")), eq(36)))
                .thenReturn(new BigDecimal("16967.64"));
        when(eligibilityService.evaluate(any(), any(), eq(new BigDecimal("16967.64"))))
                .thenReturn(new EligibilityResult(true, List.of()));
        when(offerService.generate(any(), eq(new BigDecimal("13.50")), eq(new BigDecimal("16967.64"))))
                .thenReturn(offer);

        LoanApplicationResponse response = service.process(request(720));

        assertThat(response.status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(response.riskBand()).isEqualTo(RiskBand.MEDIUM);
        assertThat(response.offer().emi()).isEqualByComparingTo("16967.64");
        assertThat(response.rejectionReasons()).isNull();

        ArgumentCaptor<LoanApplicationEntity> saved = ArgumentCaptor.forClass(LoanApplicationEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(response.applicationId());
        assertThat(saved.getValue().getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(saved.getValue().getRiskBand()).isEqualTo(RiskBand.MEDIUM);
    }

    @Test
    void rejectsIneligibleApplicationWithoutOfferAndStoresReasons() {
        List<RejectionReason> reasons = List.of(RejectionReason.CREDIT_SCORE_TOO_LOW);
        when(riskService.classify(550)).thenReturn(Optional.empty());
        when(interestRateService.calculate(eq(RiskBand.HIGH), any(), any())).thenReturn(new BigDecimal("15.00"));
        when(emiCalculator.calculate(any(), any(), eq(36))).thenReturn(new BigDecimal("17333.00"));
        when(eligibilityService.evaluate(any(), any(), any())).thenReturn(new EligibilityResult(false, reasons));

        LoanApplicationResponse response = service.process(request(550));

        assertThat(response.status()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(response.riskBand()).isNull();
        assertThat(response.offer()).isNull();
        assertThat(response.rejectionReasons()).containsExactlyElementsOf(reasons);
        verify(offerService, never()).generate(any(), any(), any());

        ArgumentCaptor<LoanApplicationEntity> saved = ArgumentCaptor.forClass(LoanApplicationEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(saved.getValue().getRejectionReasons()).containsExactlyElementsOf(reasons);
    }
}
