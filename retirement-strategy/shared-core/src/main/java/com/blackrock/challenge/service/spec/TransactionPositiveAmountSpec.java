package com.blackrock.challenge.service.spec;

import com.blackrock.challenge.domain.dto.TransactionDto;

public class TransactionPositiveAmountSpec implements Spec<TransactionDto> {
  @Override
  public boolean ok(TransactionDto t) {
    return t.amount() > 0;
  }

  @Override
  public String reason() {
    return "Amount must be > 0";
  }
}
