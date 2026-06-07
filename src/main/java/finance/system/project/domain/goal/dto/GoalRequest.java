package finance.system.project.domain.goal.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,

        @NotNull(message = "Target amount is required")
        @Positive
        BigDecimal targetAmount,

        LocalDate targetDate,
        String color,
        String icon
) {}