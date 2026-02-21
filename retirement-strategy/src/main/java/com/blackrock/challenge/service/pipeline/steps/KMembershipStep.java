package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.InvalidTransactionDto;
import com.blackrock.challenge.domain.dto.KPeriodDto;
import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * For filter endpoint: if K ranges are provided, only keep transactions that belong to at least one K.
 *
 * Optimized algorithm:
 * - Parse + sort K ranges, merge into disjoint union intervals
 * - Single scan over transactions (already sorted)
 *
 * Time: O(k log k + n)
 * Space: O(k)
 */
public class KMembershipStep implements SavingsStep {

  private record Interval(LocalDateTime start, LocalDateTime end) {}

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    if (!ctx.enforceKMembership) return ctx;
    if (ctx.k == null || ctx.k.isEmpty() || ctx.valid.isEmpty()) return ctx;

    List<Interval> intervals = new ArrayList<>(ctx.k.size());
    for (KPeriodDto kp : ctx.k) {
      intervals.add(new Interval(DateTimeUtil.parse(kp.start()), DateTimeUtil.parse(kp.end())));
    }
    intervals.sort(Comparator.comparing(Interval::start));

    // merge
    List<Interval> merged = new ArrayList<>();
    Interval cur = intervals.get(0);
    for (int i = 1; i < intervals.size(); i++) {
      Interval nxt = intervals.get(i);
      if (!nxt.start().isAfter(cur.end())) {
        // overlap or touch: extend
        cur = new Interval(cur.start(), cur.end().isAfter(nxt.end()) ? cur.end() : nxt.end());
      } else {
        merged.add(cur);
        cur = nxt;
      }
    }
    merged.add(cur);

    List<TransactionDto> kept = new ArrayList<>();
    List<LocalDateTime> keptTimes = new ArrayList<>();

    int m = 0;
    for (int i = 0; i < ctx.valid.size(); i++) {
      TransactionDto t = ctx.valid.get(i);
      LocalDateTime time = ctx.validTimes.get(i);

      while (m < merged.size() && merged.get(m).end().isBefore(time)) {
        m++;
      }

      boolean inside = m < merged.size()
          && !time.isBefore(merged.get(m).start())
          && !time.isAfter(merged.get(m).end());

      if (inside) {
        kept.add(t);
        keptTimes.add(time);
      } else {
        ctx.invalid.add(new InvalidTransactionDto(t.date(), t.amount(), t.ceiling(), t.remanent(), "Outside K ranges"));
      }
    }

    ctx.valid = kept;
    ctx.validTimes = keptTimes;
    return ctx;
  }
}
