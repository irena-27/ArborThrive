package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.PPeriodDto;
import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Applies P-period "extra" amounts.
 *
 * Optimized algorithm:
 * - Build a sorted list of events (start:+extra, end+ε:-extra)
 * - Sweep through transactions (already sorted by time) updating a running sum
 *
 * Time: O(p log p + n)
 * Space: O(p)
 */
public class ApplyPStep implements SavingsStep {

  private record Event(LocalDateTime time, double delta) {}

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    if (ctx.p == null || ctx.p.isEmpty() || ctx.valid.isEmpty()) return ctx;

    List<Event> events = new ArrayList<>(ctx.p.size() * 2);
    for (PPeriodDto pp : ctx.p) {
      LocalDateTime s = DateTimeUtil.parse(pp.start());
      LocalDateTime e = DateTimeUtil.parse(pp.end());
      events.add(new Event(s, pp.extra()));
      // inclusive end: subtract right after the end
      events.add(new Event(e.plusNanos(1), -pp.extra()));
    }
    events.sort(Comparator.comparing(Event::time));

    List<TransactionDto> out = new ArrayList<>(ctx.valid.size());

    int j = 0;
    double active = 0.0;

    for (int i = 0; i < ctx.valid.size(); i++) {
      LocalDateTime ttime = ctx.validTimes.get(i);

      while (j < events.size() && !events.get(j).time().isAfter(ttime)) {
        active += events.get(j).delta();
        j++;
      }

      TransactionDto t = ctx.valid.get(i);
      out.add(new TransactionDto(t.date(), t.amount(), t.ceiling(), t.remanent() + active));
    }

    ctx.valid = out;
    // times unchanged
    return ctx;
  }
}
