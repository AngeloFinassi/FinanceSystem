package finance.system.project.domain.goal.dto;

import finance.system.project.domain.goal.GoalStatus;
import finance.system.project.domain.user.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record GoalResponse(
        UUID id,
        String title,
        String description,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        BigDecimal remaining,
        double progress,
        LocalDate targetDate,
        GoalStatus status,
        String color,
        String icon,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
