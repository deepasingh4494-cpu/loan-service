package com.rbih.lending.service;

import com.rbih.lending.domain.enums.RiskBand;
import java.util.Optional;

public interface RiskClassificationService {
    Optional<RiskBand> classify(int creditScore);
}
