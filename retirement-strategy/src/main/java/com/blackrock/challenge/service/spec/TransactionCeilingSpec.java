package com.blackrock.challenge.service.spec;

import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.util.MoneyMath;

public class TransactionCeilingSpec implements Spec<TransactionDto> {
  @Override
  public boolean ok(TransactionDto t) {
    return t.ceiling() >= t.amount() && MoneyMath.isMultipleOf100(t.ceiling());
  }

  @Override
  public String reason() {
    return "Ceiling must be >= amount and a multiple of 100";
  }
}
