package com.rbih.lending.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.domain.enums.RejectionReason;
import com.rbih.lending.domain.enums.RiskBand;
import com.rbih.lending.dto.response.LoanApplicationResponse;
import com.rbih.lending.dto.response.OfferResponse;
import com.rbih.lending.service.LoanApplicationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean LoanApplicationService service;

    private static String body(int age, int score, String amount, int tenure, String income, String employment) {
        return """
                {"applicant":{"name":"Asha","age":%d,"monthlyIncome":%s,"employmentType":"%s","creditScore":%d},
                 "loan":{"amount":%s,"tenureMonths":%d,"purpose":"PERSONAL"}}
                """.formatted(age, income, employment, score, amount, tenure);
    }

    private String validBody() {
        return body(30, 720, "500000", 36, "75000", "SALARIED");
    }

    @Test
    void returnsApprovedDecision() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.process(any())).thenReturn(new LoanApplicationResponse(id, ApplicationStatus.APPROVED,
                RiskBand.MEDIUM,
                new OfferResponse(new BigDecimal("13.50"), 36, new BigDecimal("16967.64"), new BigDecimal("610835.04")),
                null));

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(id.toString()))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.riskBand").value("MEDIUM"))
                .andExpect(jsonPath("$.offer.interestRate").value(13.5))
                .andExpect(jsonPath("$.offer.emi").value(16967.64))
                .andExpect(jsonPath("$.rejectionReasons").doesNotExist());
    }

    @Test
    void returnsRejectedDecisionWithNullRiskBand() throws Exception {
        when(service.process(any())).thenReturn(new LoanApplicationResponse(UUID.randomUUID(),
                ApplicationStatus.REJECTED, null, null, List.of(RejectionReason.EMI_EXCEEDS_60_PERCENT)));

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.riskBand").isEmpty())
                .andExpect(content().string(containsString("\"riskBand\":null")))
                .andExpect(jsonPath("$.rejectionReasons[0]").value("EMI_EXCEEDS_60_PERCENT"))
                .andExpect(jsonPath("$.offer").doesNotExist());
    }

    @Test
    void rejectsOutOfRangeFieldsWithAllMessages() throws Exception {
        String invalid = body(19, 950, "5000", 400, "0", "SALARIED");

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details", hasItem("applicant.age: age must be between 21 and 60")))
                .andExpect(jsonPath("$.details",
                        hasItem("applicant.creditScore: creditScore must be between 300 and 900")))
                .andExpect(jsonPath("$.details",
                        hasItem("applicant.monthlyIncome: monthlyIncome must be greater than 0")))
                .andExpect(jsonPath("$.details",
                        hasItem("loan.amount: amount must be between 10,000 and 50,00,000")))
                .andExpect(jsonPath("$.details",
                        hasItem("loan.tenureMonths: tenureMonths must be between 6 and 360")));
        verifyNoInteractions(service);
    }

    @Test
    void rejectsUnknownEnumValue() throws Exception {
        String invalid = body(30, 720, "500000", 36, "75000", "FREELANCER");

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]").value(
                        "applicant.employmentType: must be one of [SALARIED, SELF_EMPLOYED]"));
    }

    @Test
    void rejectsMissingBody() throws Exception {
        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingApplicantBlock() throws Exception {
        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
