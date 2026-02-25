package com.blackrock.challenge.service.returns;

import com.blackrock.challenge.domain.dto.ReturnsRequest;
import com.blackrock.challenge.util.TaxUtil;
import org.springframework.stereotype.Component;

@Component
public class NpsReturnsCalculator extends AbstractReturnsCalculator {
  @Override
  public Instrument instrument() {
    return Instrument.NPS;
  }

  @Override
  protected double annualRate() {
    return 0.0711; // 7.11% compounded annually (per PDF)
  }

  @Override
  protected double taxBenefit(ReturnsRequest req, double invested) {
    double annualIncome = req.wage() * 12.0;
    double maxByIncome = annualIncome * 0.10;
    double deduction = Math.min(invested, Math.min(maxByIncome, 200_000.0));
    double before = TaxUtil.tax(annualIncome);
    double after = TaxUtil.tax(Math.max(0.0, annualIncome - deduction));
    return Math.max(0.0, before - after);
  }
}
