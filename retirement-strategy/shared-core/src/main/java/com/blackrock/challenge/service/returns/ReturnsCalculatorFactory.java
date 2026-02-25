package com.blackrock.challenge.service.returns;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ReturnsCalculatorFactory {
  private final Map<Instrument, ReturnsCalculator> byType = new EnumMap<>(Instrument.class);

  public ReturnsCalculatorFactory(List<ReturnsCalculator> calculators) {
    for (ReturnsCalculator c : calculators) {
      byType.put(c.instrument(), c);
    }
  }

  public ReturnsCalculator get(Instrument type) {
    ReturnsCalculator c = byType.get(type);
    if (c == null) {
      throw new IllegalArgumentException("Unsupported instrument: " + type);
    }
    return c;
  }
}
