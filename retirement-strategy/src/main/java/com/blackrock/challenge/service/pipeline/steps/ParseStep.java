package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.service.pipeline.*;
import com.blackrock.challenge.util.DateTimeUtil;
import com.blackrock.challenge.util.MoneyMath;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parses raw expenses into transactions with ceiling/remanent and sorts them by date.
 *
 * Optimization: parses timestamps exactly once and stores them in {@link SavingsContext#transactionTimes}.
 */
public class ParseStep implements SavingsStep {

  private record TxWithTime(TransactionDto tx, LocalDateTime time) {}

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    List<TxWithTime> tmp = new ArrayList<>(ctx.expenses.size());

    for (ExpenseDto e : ctx.expenses) {
      double amount = e.amount() == null ? 0 : e.amount();
      double ceiling = MoneyMath.ceilToNext100(amount);
      double remanent = amount > 0 ? (ceiling - amount) : 0;
      LocalDateTime time = DateTimeUtil.parse(e.date());
      tmp.add(new TxWithTime(new TransactionDto(e.date(), amount, ceiling, remanent), time));
    }

    tmp.sort(Comparator.comparing(TxWithTime::time));

    ctx.transactions = new ArrayList<>(tmp.size());
    ctx.transactionTimes = new ArrayList<>(tmp.size());

    for (TxWithTime t : tmp) {
      ctx.transactions.add(t.tx());
      ctx.transactionTimes.add(t.time());
    }

    // Reset downstream collections
    ctx.valid = new ArrayList<>();
    ctx.validTimes = new ArrayList<>();
    ctx.invalid = new ArrayList<>();

    return ctx;
  }
}
