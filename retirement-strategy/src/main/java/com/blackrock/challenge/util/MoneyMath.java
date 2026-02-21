package com.blackrock.challenge.util;

public final class MoneyMath {
  private MoneyMath() {}

  public static double ceilToNext100(double amount) {
    if (amount <= 0) return 0;
    return Math.ceil(amount / 100.0) * 100.0;
  }

  public static boolean isMultipleOf100(double x) {
    return Math.abs(x % 100.0) < 1e-9;
  }
}
