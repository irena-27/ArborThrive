package com.blackrock.challenge.service.pipeline;

public interface SavingsStep {
  SavingsContext apply(SavingsContext ctx);
}
