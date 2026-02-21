package com.blackrock.challenge.util;

public final class TaxUtil {
  private TaxUtil() {}

  // Simplified slabs from the PDF text
  public static double tax(double annualIncome) {
    double tax = 0.0;
    if (annualIncome <= 700_000) return 0.0;

    // 7L - 10L => 10%
    if (annualIncome > 700_000) {
      double upper = Math.min(annualIncome, 1_000_000);
      tax += Math.max(0, upper - 700_000) * 0.10;
    }
    // 10L - 12L => 15%
    if (annualIncome > 1_000_000) {
      double upper = Math.min(annualIncome, 1_200_000);
      tax += Math.max(0, upper - 1_000_000) * 0.15;
    }
    // 12L - 15L => 20%
    if (annualIncome > 1_200_000) {
      double upper = Math.min(annualIncome, 1_500_000);
      tax += Math.max(0, upper - 1_200_000) * 0.20;
    }
    // > 15L => 30%
    if (annualIncome > 1_500_000) {
      tax += (annualIncome - 1_500_000) * 0.30;
    }

    return tax;
  }
}
