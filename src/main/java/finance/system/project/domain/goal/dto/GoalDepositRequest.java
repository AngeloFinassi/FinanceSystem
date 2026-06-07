package finance.system.project.domain.goal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record GoalDepositRequest(
        @NotNull
        @Positive
        BigDecimal amount
) {
}
