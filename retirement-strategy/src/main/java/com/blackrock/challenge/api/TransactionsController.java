package com.blackrock.challenge.api;

import com.blackrock.challenge.domain.dto.*;
import com.blackrock.challenge.cqrs.command.CommandFacade;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionsController {
  private final CommandFacade commands;

  public TransactionsController(CommandFacade commands) {
    this.commands = commands;
  }

  @PostMapping("/transactions:parse")
  public List<TransactionDto> parse(@RequestBody @Valid List<ExpenseDto> expenses) {
    return commands.parse(expenses);
  }

  @PostMapping("/transactions:validator")
  public ValidatorResponse validator(@RequestBody @Valid ValidatorRequest req) {
    return commands.validate(req.wage(), req.transactions());
  }

  @PostMapping("/transactions:filter")
  public TemporalFilterResponse filter(@RequestBody @Valid TemporalFilterRequest req) {
    return commands.filter(req.q(), req.p(), req.k(), req.transactions());
  }
}
