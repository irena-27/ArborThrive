package com.blackrock.challenge.service.spec;

import com.blackrock.challenge.domain.dto.TransactionDto;

public class TransactionRemanentSpec implements Spec<TransactionDto> {
  @Override
  public boolean ok(TransactionDto t) {
    return t.remanent() >= 0 && Math.abs((t.ceiling() - t.amount()) - t.remanent()) < 1e-6;
  }

  @Override
  public String reason() {
    return "Remanent must equal (ceiling - amount) and be non-negative";
  }
}
