package com.blackrock.challenge.cqrs.command;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsPipeline;
import com.blackrock.challenge.service.pipeline.steps.*;
import com.blackrock.challenge.store.audit.CommandAuditStore;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CommandFacade {
  private final CommandAuditStore auditStore;

  private final SavingsPipeline parsePipeline = new SavingsPipeline(List.of(new ParseStep()));

  // Keep ParseStep here because validator now accepts raw ExpenseDto
  private final SavingsPipeline validatorPipeline =
          new SavingsPipeline(List.of(new ParseStep(), new ValidateStep()));

  // Keep ParseStep here because filter now accepts raw ExpenseDto
  private final SavingsPipeline filterPipeline =
          new SavingsPipeline(
                  List.of(
                          new ParseStep(),
                          new ValidateStep(),
                          new KMembershipStep(),
                          new ApplyQStep(),
                          new ApplyPStep()));

  public CommandFacade(CommandAuditStore auditStore) {
    this.auditStore = auditStore;
  }

  public List<TransactionDto> parse(List<ExpenseDto> expenses) {
    SavingsContext ctx = new SavingsContext();
    ctx.expenses = expenses;
    parsePipeline.run(ctx);
    auditStore.append(
            "transactions:parse",
            Map.of(
                    "inputExpenses", expenses == null ? 0 : expenses.size(),
                    "parsedTransactions", ctx.transactions == null ? 0 : ctx.transactions.size()));
    return ctx.transactions;
  }

  public ValidatorResponse validate(double wage, List<ExpenseDto> expenses) {
    SavingsContext ctx = new SavingsContext();
    ctx.wage = wage;
    ctx.expenses = expenses;
    validatorPipeline.run(ctx);
    auditStore.append(
            "transactions:validator",
            Map.of(
                    "valid", ctx.valid == null ? 0 : ctx.valid.size(),
                    "invalid", ctx.invalid == null ? 0 : ctx.invalid.size()));
    return new ValidatorResponse(ctx.valid, ctx.invalid);
  }

  public TemporalFilterResponse filter(
          List<QPeriodDto> q, List<PPeriodDto> p, List<KPeriodDto> k, List<ExpenseDto> expenses) {
    SavingsContext ctx = new SavingsContext();
    ctx.q = q;
    ctx.p = p;
    ctx.k = k;
    ctx.expenses = expenses;
    ctx.enforceKMembership = true; // ✅ important
    filterPipeline.run(ctx);

    auditStore.append(
            "transactions:filter",
            Map.of(
                    "valid", ctx.valid == null ? 0 : ctx.valid.size(),
                    "invalid", ctx.invalid == null ? 0 : ctx.invalid.size(),
                    "kWindows", ctx.k == null ? 0 : ctx.k.size()));

    return new TemporalFilterResponse(ctx.valid, ctx.invalid);
  }
}