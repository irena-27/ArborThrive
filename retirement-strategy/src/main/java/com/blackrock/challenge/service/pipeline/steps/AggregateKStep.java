package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.KPeriodDto;
import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates remanent savings per K-range using prefix sums + binary search.
 *
 * Time: O(n + k log n)
 * Space: O(n)
 */
public class AggregateKStep implements SavingsStep {
  @Override
  public SavingsContext apply(SavingsContext ctx) {
    ctx.savingsByK = new ArrayList<>();
    if (ctx.k == null || ctx.k.isEmpty()) {
      return ctx;
    }

    List<TransactionDto> txs = ctx.valid;
    List<LocalDateTime> dates = ctx.validTimes;

    double[] prefix = new double[txs.size() + 1];
    for (int i = 0; i < txs.size(); i++) {
      prefix[i + 1] = prefix[i] + txs.get(i).remanent();
    }

    for (KPeriodDto kp : ctx.k) {
      LocalDateTime s = DateTimeUtil.parse(kp.start());
      LocalDateTime e = DateTimeUtil.parse(kp.end());
      int left = lowerBound(dates, s);
      int right = upperBound(dates, e);
      ctx.savingsByK.add(prefix[right] - prefix[left]);
    }

    return ctx;
  }

  private static int lowerBound(List<LocalDateTime> a, LocalDateTime x) {
    int lo = 0, hi = a.size();
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (a.get(mid).isBefore(x)) lo = mid + 1;
      else hi = mid;
    }
    return lo;
  }

  private static int upperBound(List<LocalDateTime> a, LocalDateTime x) {
    int lo = 0, hi = a.size();
    while (lo < hi) {
      int mid = (lo + hi) >>> 1;
      if (!a.get(mid).isAfter(x)) lo = mid + 1;
      else hi = mid;
    }
    return lo;
  }
}
