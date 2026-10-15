package com.rbih.lending;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rbih.lending.domain.enums.ApplicationStatus;
import com.rbih.lending.repository.LoanApplicationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end through real beans and the in-memory H2 audit store. */
@SpringBootTest
@AutoConfigureMockMvc
class LoanApplicationFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired LoanApplicationRepository repository;

    private static String body(int age, int score, int amount, int tenure, int income, String employment) {
        return """
                {"applicant":{"name":"Asha","age":%d,"monthlyIncome":%d,"employmentType":"%s","creditScore":%d},
                 "loan":{"amount":%d,"tenureMonths":%d,"purpose":"PERSONAL"}}
                """.formatted(age, income, employment, score, amount, tenure);
    }

    @Test
    void approvesAndPersistsEligibleApplication() throws Exception {
        long before = repository.count();

        // MEDIUM band, salaried, 5,00,000 -> 13.5% -> EMI 16,967.64
        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content(body(30, 720, 500000, 36, 75000, "SALARIED")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.riskBand").value("MEDIUM"))
                .andExpect(jsonPath("$.offer.interestRate").value(13.5))
                .andExpect(jsonPath("$.offer.tenureMonths").value(36))
                .andExpect(jsonPath("$.offer.emi").value(16967.64))
                .andExpect(jsonPath("$.offer.totalPayable").value(610835.04));

        assertThat(repository.count()).isEqualTo(before + 1);
    }

    @Test
    void rejectsWithAllReasonsAndPersists() throws Exception {
        long before = repository.count();

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content(body(60, 550, 5000000, 120, 20000, "SELF_EMPLOYED")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.riskBand").isEmpty())
                .andExpect(jsonPath("$.rejectionReasons.length()").value(3));

        assertThat(repository.count()).isEqualTo(before + 1);
        assertThat(repository.findAll()).anyMatch(e -> e.getStatus() == ApplicationStatus.REJECTED
                && e.getRejectionReasons().size() == 3);
    }

    @Test
    void invalidRequestIsNotPersisted() throws Exception {
        long before = repository.count();

        mockMvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content(body(18, 720, 500000, 36, 75000, "SALARIED")))
                .andExpect(status().isBadRequest());

        assertThat(repository.count()).isEqualTo(before);
    }
}
