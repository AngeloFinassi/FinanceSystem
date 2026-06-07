package finance.system.project.domain.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remaining,     // limitAmount - spentAmount
        double percentageUsed,
        Integer month,
        Integer year
) {}