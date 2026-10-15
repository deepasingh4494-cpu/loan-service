package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbih.lending.domain.enums.LoanPurpose;
import com.rbih.lending.domain.model.Loan;
import com.rbih.lending.domain.model.Offer;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OfferServiceTest {

    private final OfferService service = new OfferServiceImpl();

    @Test
    void buildsSingleOfferForRequestedTenure() {
        Loan loan = new Loan(new BigDecimal("500000"), 36, LoanPurpose.PERSONAL);

        Offer offer = service.generate(loan, new BigDecimal("13.50"), new BigDecimal("16967.64"));

        assertThat(offer.interestRate()).isEqualByComparingTo("13.50");
        assertThat(offer.tenureMonths()).isEqualTo(36);
        assertThat(offer.emi()).isEqualByComparingTo("16967.64");
        assertThat(offer.totalPayable()).isEqualByComparingTo("610835.04");
        assertThat(offer.totalPayable().scale()).isEqualTo(2);
    }
}
