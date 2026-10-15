package com.rbih.lending.service;

import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.domain.model.Offer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class OfferServiceImpl implements OfferService {

    @Override
    public Offer generate(Loan loan, BigDecimal interestRate, BigDecimal emi) {
        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(loan.tenureMonths()))
                .setScale(2, RoundingMode.HALF_UP);
        return new Offer(interestRate, loan.tenureMonths(), emi, totalPayable);
    }
}
