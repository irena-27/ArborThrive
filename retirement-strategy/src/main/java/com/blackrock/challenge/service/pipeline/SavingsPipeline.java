package com.blackrock.challenge.service.pipeline;

import java.util.List;

public class SavingsPipeline {
  private final List<SavingsStep> steps;

  public SavingsPipeline(List<SavingsStep> steps) {
    this.steps = steps;
  }

  public SavingsContext run(SavingsContext ctx) {
    for (SavingsStep s : steps) {
      ctx = s.apply(ctx);
    }
    return ctx;
  }
}
