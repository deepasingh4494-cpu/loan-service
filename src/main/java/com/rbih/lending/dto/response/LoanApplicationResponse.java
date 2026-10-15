package com.rbih.lending.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.enums.RiskBand;
import java.util.List;
import java.util.UUID;


public record LoanApplicationResponse(UUID applicationId,
                                      ApplicationStatus status,
                                      RiskBand riskBand,
                                      @JsonInclude(JsonInclude.Include.NON_NULL) OfferResponse offer,
                                      @JsonInclude(JsonInclude.Include.NON_NULL) List<RejectionReason> rejectionReasons) {
}
