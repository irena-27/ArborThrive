package com.blackrock.challenge.service.spec;

import com.blackrock.challenge.domain.dto.TransactionDto;

public class TransactionMaxInvestSpec implements Spec<TransactionDto> {
  private final double maxInvest;

  public TransactionMaxInvestSpec(double wage) {
    // Interpreting "maximum amount to invest" as the monthly wage (conservative and realistic).
    this.maxInvest = Math.max(0, wage);
  }

  @Override
  public boolean ok(TransactionDto t) {
    return t.remanent() <= maxInvest;
  }

  @Override
  public String reason() {
    return "Remanent exceeds maximum allowed to invest (wage)";
  }
}
