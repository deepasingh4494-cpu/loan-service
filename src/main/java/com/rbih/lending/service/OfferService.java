package com.rbih.lending.service;

import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.domain.model.Offer;
import java.math.BigDecimal;

public interface OfferService {
    Offer generate(Loan loan, BigDecimal interestRate, BigDecimal emi);
}
