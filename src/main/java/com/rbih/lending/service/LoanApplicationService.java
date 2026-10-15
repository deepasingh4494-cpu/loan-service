package com.rbih.lending.service;

import com.rbih.lending.dto.request.LoanApplicationRequest;
import com.rbih.lending.dto.response.LoanApplicationResponse;

public interface LoanApplicationService {
    LoanApplicationResponse process(LoanApplicationRequest request);
}
