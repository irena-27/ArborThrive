package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.InvalidTransactionDto;
import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.service.spec.*;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Validates transactions using composable Specifications and splits into valid/invalid.
 *
 * Time/space:
 * - O(n) time (with O(n) hash set for duplicate detection)
 * - O(n) space for seen keys + output lists
 */
public class ValidateStep implements SavingsStep {

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    // Ensure timestamps are available (validator/filter endpoints can skip ParseStep)
    if (ctx.transactionTimes == null || ctx.transactionTimes.size() != ctx.transactions.size()) {
      ctx.transactionTimes = new ArrayList<>(ctx.transactions.size());
      for (TransactionDto t : ctx.transactions) {
        ctx.transactionTimes.add(DateTimeUtil.parse(t.date()));
      }
    }

    // no wage => treat all as valid
    if (ctx.wage == null) {
      ctx.valid = new ArrayList<>(ctx.transactions);
      ctx.validTimes = new ArrayList<>(ctx.transactionTimes);
      return ctx;
    }

    List<Spec<TransactionDto>> specs = List.of(
        new TransactionPositiveAmountSpec(),
        new TransactionCeilingSpec(),
        new TransactionRemanentSpec(),
        new TransactionMaxInvestSpec(ctx.wage)
    );

    Set<String> seen = new HashSet<>(Math.max(16, ctx.transactions.size() * 2));

    for (int i = 0; i < ctx.transactions.size(); i++) {
      TransactionDto t = ctx.transactions.get(i);
      LocalDateTime time = ctx.transactionTimes.get(i);

      String key = t.date() + "|" + t.amount();
      if (!seen.add(key)) {
        ctx.invalid.add(new InvalidTransactionDto(t.date(), t.amount(), t.ceiling(), t.remanent(), "Duplicate transaction"));
        continue;
      }

      String fail = null;
      for (Spec<TransactionDto> s : specs) {
        if (!s.ok(t)) {
          fail = s.reason();
          break;
        }
      }

      // If no spec failed, the transaction is valid.
      // (No need to build a composite AndSpec: we already evaluated all specs above.)
      if (fail == null) {
        ctx.valid.add(t);
        ctx.validTimes.add(time);
      } else {
        ctx.invalid.add(new InvalidTransactionDto(t.date(), t.amount(), t.ceiling(), t.remanent(), fail != null ? fail : "Invalid transaction"));
      }
    }

    return ctx;
  }
}
