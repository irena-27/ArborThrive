package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ensures {@link SavingsContext#transactions} are sorted by time and timestamps are parsed once.
 * Useful when the endpoint receives already-parsed TransactionDto objects (validator/filter endpoints).
 */
public class NormalizeTransactionsStep implements SavingsStep {

  private record TxWithTime(TransactionDto tx, LocalDateTime time) {}

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    if (ctx.transactions == null) {
      ctx.transactions = List.of();
      ctx.transactionTimes = List.of();
      return ctx;
    }

    List<TxWithTime> tmp = new ArrayList<>(ctx.transactions.size());
    for (TransactionDto t : ctx.transactions) {
      tmp.add(new TxWithTime(t, DateTimeUtil.parse(t.date())));
    }
    tmp.sort(Comparator.comparing(TxWithTime::time));

    List<TransactionDto> sortedTx = new ArrayList<>(tmp.size());
    List<LocalDateTime> sortedTimes = new ArrayList<>(tmp.size());
    for (TxWithTime twt : tmp) {
      sortedTx.add(twt.tx());
      sortedTimes.add(twt.time());
    }

    ctx.transactions = sortedTx;
    ctx.transactionTimes = sortedTimes;

    // reset downstream
    ctx.valid = new ArrayList<>();
    ctx.validTimes = new ArrayList<>();
    ctx.invalid = new ArrayList<>();

    return ctx;
  }
}
