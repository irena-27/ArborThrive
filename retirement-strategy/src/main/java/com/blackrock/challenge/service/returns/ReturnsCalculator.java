package com.blackrock.challenge.service.returns;

import com.blackrock.challenge.domain.dto.ReturnsRequest;
import com.blackrock.challenge.domain.dto.ReturnsResponse;

public interface ReturnsCalculator {
  Instrument instrument();
  ReturnsResponse calculate(ReturnsRequest req, ComputedSavings computed);
}
