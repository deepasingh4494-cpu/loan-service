package com.rbih.lending.domain.model;

import com.rbih.lending.domain.enums.RejectionReason;
import java.util.List;

public record EligibilityResult(boolean eligible, List<RejectionReason> reasons) {
}
