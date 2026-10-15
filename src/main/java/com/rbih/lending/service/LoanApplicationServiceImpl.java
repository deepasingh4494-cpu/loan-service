package com.rbih.lending.service;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.RiskBand;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.EligibilityResult;
import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.domain.model.Offer;
import com.rbih.lending.dto.request.LoanApplicationRequest;
import com.rbih.lending.dto.response.LoanApplicationResponse;
import com.rbih.lending.mapper.LoanApplicationMapper;
import com.rbih.lending.repository.LoanApplicationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final RiskClassificationService riskClassificationService;
    private final InterestRateService interestRateService;
    private final EmiCalculator emiCalculator;
    private final EligibilityService eligibilityService;
    private final OfferService offerService;
    private final LoanApplicationRepository repository;
    private final LoanApplicationMapper mapper;

    public LoanApplicationServiceImpl(RiskClassificationService riskClassificationService,
                                      InterestRateService interestRateService,
                                      EmiCalculator emiCalculator,
                                      EligibilityService eligibilityService,
                                      OfferService offerService,
                                      LoanApplicationRepository repository,
                                      LoanApplicationMapper mapper) {
        this.riskClassificationService = riskClassificationService;
        this.interestRateService = interestRateService;
        this.emiCalculator = emiCalculator;
        this.eligibilityService = eligibilityService;
        this.offerService = offerService;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public LoanApplicationResponse process(LoanApplicationRequest request) {
        Applicant applicant = mapper.toApplicant(request);
        Loan loan = mapper.toLoan(request);
        UUID applicationId = UUID.randomUUID();

        Optional<RiskBand> riskBand = riskClassificationService.classify(applicant.creditScore());
        RiskBand pricingBand = riskBand.orElse(RiskBand.HIGH);
        BigDecimal rate = interestRateService.calculate(pricingBand, applicant.employmentType(), loan.amount());
        BigDecimal emi = emiCalculator.calculate(loan.amount(), rate, loan.tenureMonths());

        EligibilityResult eligibility = eligibilityService.evaluate(applicant, loan, emi);

        if (eligibility.eligible()) {
            Offer offer = offerService.generate(loan, rate, emi);
            repository.save(mapper.toEntity(applicationId, applicant, loan, ApplicationStatus.APPROVED,
                    riskBand.orElse(null), offer, List.of()));
            return mapper.toApprovedResponse(applicationId, riskBand.orElseThrow(), offer);
        }

        repository.save(mapper.toEntity(applicationId, applicant, loan, ApplicationStatus.REJECTED,
                riskBand.orElse(null), null, eligibility.reasons()));
        return mapper.toRejectedResponse(applicationId, eligibility.reasons());
    }
}
