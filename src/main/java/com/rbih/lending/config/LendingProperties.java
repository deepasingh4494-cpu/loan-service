package com.rbih.lending.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lending")
public record LendingProperties(BigDecimal baseRatePercent) {
}
