package com.rbih.lending.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LoanApplicationRequest(
        @NotNull @Valid ApplicantRequest applicant,
        @NotNull @Valid LoanRequest loan) {
}
