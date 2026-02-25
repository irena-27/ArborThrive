package com.blackrock.challenge.api;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.cqrs.query.QueryFacade;
import com.blackrock.challenge.service.returns.Instrument;
import jakarta.validation.Valid;
import java.util.concurrent.CompletionStage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {
  private final QueryFacade queries;

  public ReturnsController(QueryFacade queries) {
    this.queries = queries;
  }

  @PostMapping("/returns:nps")
  public CompletionStage<ReturnsResponse> returnsNps(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody @Valid ReturnsRequest req) {
    return queries.returns(Instrument.NPS, req, idempotencyKey);
  }

  @PostMapping("/returns:index")
  public CompletionStage<ReturnsResponse> returnsIndex(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
      @RequestBody @Valid ReturnsRequest req) {
    return queries.returns(Instrument.INDEX, req, idempotencyKey);
  }

  @GetMapping("/performance")
  public PerformanceResponse performance() {
    return queries.performance();
  }
}
