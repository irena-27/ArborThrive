package com.blackrock.challenge.service.pipeline;

import com.blackrock.challenge.domain.dto.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SavingsContext {
  public List<ExpenseDto> expenses = new ArrayList<>();

  /** Parsed transactions (sorted by time ascending). */
  public List<TransactionDto> transactions = new ArrayList<>();

  /** Parsed timestamps aligned with {@link #transactions}. */
  public List<LocalDateTime> transactionTimes = new ArrayList<>();

  /** Valid transactions after validation/filters (sorted by time ascending). */
  public List<TransactionDto> valid = new ArrayList<>();

  /** Parsed timestamps aligned with {@link #valid}. */
  public List<LocalDateTime> validTimes = new ArrayList<>();

  /** Invalid transactions with reasons. */
  public List<InvalidTransactionDto> invalid = new ArrayList<>();

  public Double wage;
  public List<QPeriodDto> q = List.of();
  public List<PPeriodDto> p = List.of();
  public List<KPeriodDto> k = List.of();

  /** Aggregation output: savings amounts aligned with k list order */
  public List<Double> savingsByK = new ArrayList<>();

  /**
   * For filter endpoint only: if true and k is provided, a transaction must belong to at least one k range.
   */
  public boolean enforceKMembership = false;
}
