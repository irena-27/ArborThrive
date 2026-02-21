package com.blackrock.challenge.cqrs.command;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsPipeline;
import com.blackrock.challenge.service.pipeline.steps.*;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CQRS-lite Command side: mutating/transforming operations (parse/validate/filter).
 *
 * Note: there is no persistence in this hackathon API, but separating command/query concerns
 * makes it easier to scale compute-heavy queries independently later.
 */
@Service
public class CommandFacade {

  private final SavingsPipeline parsePipeline = new SavingsPipeline(List.of(
      new ParseStep()
  ));

  private final SavingsPipeline validatorPipeline = new SavingsPipeline(List.of(
      new NormalizeTransactionsStep(),
      new ValidateStep()
  ));

  private final SavingsPipeline filterPipeline = new SavingsPipeline(List.of(
      new NormalizeTransactionsStep(),
      new ValidateStep(),
      new ApplyQStep(),
      new ApplyPStep(),
      new KMembershipStep()
  ));

  public List<TransactionDto> parse(List<ExpenseDto> expenses) {
    SavingsContext ctx = new SavingsContext();
    ctx.expenses = expenses;
    parsePipeline.run(ctx);
    return ctx.transactions;
  }

  public ValidatorResponse validate(Double wage, List<TransactionDto> transactions) {
    SavingsContext ctx = new SavingsContext();
    ctx.wage = wage;
    ctx.transactions = transactions;
    validatorPipeline.run(ctx);
    return new ValidatorResponse(ctx.valid, ctx.invalid);
  }

  public TemporalFilterResponse filter(
      List<QPeriodDto> q,
      List<PPeriodDto> p,
      List<KPeriodDto> k,
      List<TransactionDto> transactions) {

    SavingsContext ctx = new SavingsContext();
    ctx.q = q == null ? List.of() : q;
    ctx.p = p == null ? List.of() : p;
    ctx.k = k == null ? List.of() : k;

    // spec doesn't provide wage for filter; skip wage-based rules but keep structure.
    ctx.wage = null;

    // Enforce K membership for filter endpoint (per problem statement intent)
    ctx.enforceKMembership = true;

    ctx.transactions = transactions;
    filterPipeline.run(ctx);
    return new TemporalFilterResponse(ctx.valid, ctx.invalid);
  }
}
