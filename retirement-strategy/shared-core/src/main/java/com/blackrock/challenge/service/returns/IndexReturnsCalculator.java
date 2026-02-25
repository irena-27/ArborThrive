package com.blackrock.challenge.service.returns;

import com.blackrock.challenge.domain.dto.ReturnsRequest;
import org.springframework.stereotype.Component;

@Component
public class IndexReturnsCalculator extends AbstractReturnsCalculator {
  @Override
  public Instrument instrument() {
    return Instrument.INDEX;
  }

  @Override
  protected double annualRate() {
    return 0.1449; // 14.49% compounded annually (per PDF)
  }

  @Override
  protected double taxBenefit(ReturnsRequest req, double invested) {
    return 0.0;
  }
}
