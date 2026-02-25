package com.blackrock.challenge.service.returns;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractReturnsCalculator implements ReturnsCalculator {

  protected abstract double annualRate(); // e.g. 0.0711
  protected abstract double taxBenefit(ReturnsRequest req, double invested);

  @Override
  public ReturnsResponse calculate(ReturnsRequest req, ComputedSavings computed) {
    int age = req.age();
    double inflationRate = req.inflation() / 100.0;
    int years = Math.max(0, 60 - age);

    List<SavingsByDateDto> list = new ArrayList<>();
    for (int i=0;i<computed.kPeriods().size();i++) {
      KPeriodDto kp = computed.kPeriods().get(i);
      double invested = computed.amountsByK().get(i);

      double fv = invested * Math.pow(1.0 + annualRate(), years);
      double real = years == 0 ? fv : (fv / Math.pow(1.0 + inflationRate, years));
      double profits = real - invested;

      double tax = taxBenefit(req, invested);

      list.add(new SavingsByDateDto(kp.start(), kp.end(), invested, profits, tax));
    }

    return new ReturnsResponse(computed.totalAmount(), computed.totalCeiling(), list);
  }
}
