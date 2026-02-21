package com.blackrock.challenge.cqrs.query;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.service.RequestMetrics;
import com.blackrock.challenge.service.idempotency.IdempotencyService;
import com.blackrock.challenge.service.pipeline.SavingsContext;
import com.blackrock.challenge.service.pipeline.SavingsPipeline;
import com.blackrock.challenge.service.pipeline.steps.*;
import com.blackrock.challenge.service.returns.*;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * CQRS-lite Query side: compute-heavy projections (returns/performance).
 */
@Service
public class QueryFacade {

  private final ReturnsCalculatorFactory calculatorFactory;
  private final IdempotencyService idempotency;
  private final RequestMetrics metrics;
  private final Executor returnsExecutor;

  private final SavingsPipeline returnsPipeline = new SavingsPipeline(List.of(
      new ParseStep(),
      new ValidateStep(),
      new ApplyQStep(),
      new ApplyPStep(),
      new AggregateKStep()
  ));

  public QueryFacade(
      ReturnsCalculatorFactory calculatorFactory,
      IdempotencyService idempotency,
      RequestMetrics metrics,
      @Qualifier("returnsExecutor") Executor returnsExecutor) {
    this.calculatorFactory = calculatorFactory;
    this.idempotency = idempotency;
    this.metrics = metrics;
    this.returnsExecutor = returnsExecutor;
  }

  @TimeLimiter(name = "returns")
  @RateLimiter(name = "returns")
  @Bulkhead(name = "returns", type = Bulkhead.Type.SEMAPHORE)
  public CompletionStage<ReturnsResponse> returns(Instrument instrument, ReturnsRequest req, String idempotencyKey) {
    Object cached = idempotency.getIfPresent(idempotencyKey);
    if (cached instanceof ReturnsResponse rr) {
      return CompletableFuture.completedFuture(rr);
    }

    return CompletableFuture.supplyAsync(() -> {
      SavingsContext ctx = new SavingsContext();
      ctx.expenses = req.transactions();
      ctx.wage = req.wage();
      ctx.q = req.q() == null ? List.of() : req.q();
      ctx.p = req.p() == null ? List.of() : req.p();
      ctx.k = req.k() == null ? List.of() : req.k();

      returnsPipeline.run(ctx);

      // totals are based on all parsed transactions (before filtering)
      double totalAmount = 0.0;
      double totalCeiling = 0.0;
      for (TransactionDto t : ctx.transactions) {
        totalAmount += t.amount();
        totalCeiling += t.ceiling();
      }

      ComputedSavings computed = new ComputedSavings(
          totalAmount,
          totalCeiling,
          ctx.k,
          ctx.savingsByK
      );

      ReturnsCalculator calculator = calculatorFactory.get(instrument);
      ReturnsResponse response = calculator.calculate(req, computed);

      idempotency.put(idempotencyKey, response);
      return response;
    }, returnsExecutor);
  }

  public PerformanceResponse performance() {
    long last = metrics.getLastDurationMillis();
    Runtime rt = Runtime.getRuntime();
    long used = rt.totalMemory() - rt.freeMemory();

    String time = "lastRequestMs=" + last;
    String memory = "usedMB=" + (used / (1024 * 1024)) + ", totalMB=" + (rt.totalMemory() / (1024 * 1024));
    int threads = Thread.activeCount();

    return new PerformanceResponse(time, memory, threads);
  }
}
