package com.blackrock.challenge.service.pipeline.steps;

import com.blackrock.challenge.domain.dto.QPeriodDto;
import com.blackrock.challenge.domain.dto.TransactionDto;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsStep;
import com.blackrock.challenge.util.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Applies Q-period "fixed" remanent overrides.
 *
 * Requirement: if multiple q-periods overlap a transaction date, pick the one with the latest start.
 * If starts are equal, keep the earliest in input order.
 *
 * Optimized algorithm:
 * - Sort q-periods by start time
 * - Sweep transactions (already sorted by time)
 * - Maintain a max-heap of active intervals keyed by (start desc, order asc)
 *
 * Time: O(q log q + n log q)
 * Space: O(q)
 */
public class ApplyQStep implements SavingsStep {

  private record Qp(LocalDateTime start, LocalDateTime end, double fixed, int order) {}

  @Override
  public SavingsContext apply(SavingsContext ctx) {
    if (ctx.q == null || ctx.q.isEmpty() || ctx.valid.isEmpty()) return ctx;

    List<Qp> qps = new ArrayList<>(ctx.q.size());
    for (int i = 0; i < ctx.q.size(); i++) {
      QPeriodDto qp = ctx.q.get(i);
      qps.add(new Qp(DateTimeUtil.parse(qp.start()), DateTimeUtil.parse(qp.end()), qp.fixed(), i));
    }
    qps.sort(Comparator.comparing(Qp::start));

    PriorityQueue<Qp> active = new PriorityQueue<>((a, b) -> {
      int c = b.start.compareTo(a.start); // latest start first
      if (c != 0) return c;
      return Integer.compare(a.order, b.order); // tie: earlier order first
    });

    List<TransactionDto> out = new ArrayList<>(ctx.valid.size());

    int j = 0;
    for (int i = 0; i < ctx.valid.size(); i++) {
      LocalDateTime ttime = ctx.validTimes.get(i);

      while (j < qps.size() && !qps.get(j).start().isAfter(ttime)) {
        active.add(qps.get(j));
        j++;
      }

      // remove expired
      while (!active.isEmpty() && active.peek().end().isBefore(ttime)) {
        active.poll();
      }

      TransactionDto t = ctx.valid.get(i);
      if (active.isEmpty() || active.peek().end().isBefore(ttime) || active.peek().start().isAfter(ttime)) {
        out.add(t);
      } else {
        out.add(new TransactionDto(t.date(), t.amount(), t.ceiling(), active.peek().fixed()));
      }
    }

    ctx.valid = out;
    // times unchanged
    return ctx;
  }
}
