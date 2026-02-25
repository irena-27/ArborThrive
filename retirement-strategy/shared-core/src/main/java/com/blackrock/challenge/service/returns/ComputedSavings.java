package com.blackrock.challenge.service.returns;

import com.blackrock.challenge.domain.dto.KPeriodDto;
import java.util.List;

public record ComputedSavings(
    double totalAmount,
    double totalCeiling,
    List<KPeriodDto> kPeriods,
    List<Double> amountsByK
) {}
