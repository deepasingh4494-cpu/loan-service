package com.rbih.lending.mapper;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.enums.RiskBand;
import com.rbih.lending.domain.model.Applicant;
import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.domain.model.Offer;
import com.rbih.lending.dto.request.LoanApplicationRequest;
import com.rbih.lending.dto.response.LoanApplicationResponse;
import com.rbih.lending.dto.response.OfferResponse;
import com.rbih.lending.entity.LoanApplicationEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public Applicant toApplicant(LoanApplicationRequest request) {
        var a = request.applicant();
        return new Applicant(a.name(), a.age(), a.monthlyIncome(), a.employmentType(), a.creditScore());
    }

    public Loan toLoan(LoanApplicationRequest request) {
        var l = request.loan();
        return new Loan(l.amount(), l.tenureMonths(), l.purpose());
    }

    /** Builds the audit record. {@code offer} is null for rejections; {@code riskBand} is kept when known. */
    public LoanApplicationEntity toEntity(UUID id, Applicant applicant, Loan loan, ApplicationStatus status,
                                          RiskBand riskBand, Offer offer, List<RejectionReason> reasons) {
        return new LoanApplicationEntity(id, applicant.name(), applicant.age(), applicant.monthlyIncome(),
                applicant.employmentType(), applicant.creditScore(), loan.amount(), loan.tenureMonths(),
                loan.purpose(), status, riskBand,
                offer == null ? null : offer.interestRate(),
                offer == null ? null : offer.emi(),
                offer == null ? null : offer.totalPayable(),
                reasons, Instant.now());
    }

    public LoanApplicationResponse toApprovedResponse(UUID id, RiskBand riskBand, Offer offer) {
        OfferResponse offerResponse = new OfferResponse(
                offer.interestRate(), offer.tenureMonths(), offer.emi(), offer.totalPayable());
        return new LoanApplicationResponse(id, ApplicationStatus.APPROVED, riskBand, offerResponse, null);
    }

    public LoanApplicationResponse toRejectedResponse(UUID id, List<RejectionReason> reasons) {
        return new LoanApplicationResponse(id, ApplicationStatus.REJECTED, null, null, reasons);
    }
}
