package com.rbih.lending.service;

import com.rbih.lending.domain.enums.RiskBand;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RiskClassificationServiceImpl implements RiskClassificationService {

    private static final int LOW_RISK_MIN_SCORE = 750;
    private static final int MEDIUM_RISK_MIN_SCORE = 650;
    private static final int HIGH_RISK_MIN_SCORE = 600;

    @Override
    public Optional<RiskBand> classify(int creditScore) {
        if (creditScore >= LOW_RISK_MIN_SCORE) {
            return Optional.of(RiskBand.LOW);
        }
        if (creditScore >= MEDIUM_RISK_MIN_SCORE) {
            return Optional.of(RiskBand.MEDIUM);
        }
        if (creditScore >= HIGH_RISK_MIN_SCORE) {
            return Optional.of(RiskBand.HIGH);
        }
        return Optional.empty();
    }
}
