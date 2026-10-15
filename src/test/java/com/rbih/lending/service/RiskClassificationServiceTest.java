package com.rbih.lending.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbih.lending.domain.enums.RiskBand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiskClassificationServiceTest {

    private final RiskClassificationService service = new RiskClassificationServiceImpl();

    @ParameterizedTest
    @CsvSource({
            "900, LOW",
            "750, LOW",
            "749, MEDIUM",
            "700, MEDIUM",
            "650, MEDIUM",
            "649, HIGH",
            "600, HIGH"
    })
    void classifiesScoreIntoRiskBand(int score, RiskBand expected) {
        assertThat(service.classify(score)).contains(expected);
    }

    @Test
    void returnsNoBandBelowMinimumScore() {
        assertThat(service.classify(599)).isEmpty();
        assertThat(service.classify(300)).isEmpty();
    }
}
