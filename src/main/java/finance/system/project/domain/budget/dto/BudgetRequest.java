package finance.system.project.domain.budget.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record BudgetRequest(

        @NotNull(message = "Category is required")
        UUID categoryId,

        @NotNull(message = "Limit amount is required")
        @Positive(message = "Limit must be positive")
        BigDecimal limitAmount,

        @NotNull(message = "Month is required")
        @Min(1) @Max(12)
        Integer month,

        @NotNull(message = "Year is required")
        @Min(2000)
        Integer year
) {}