package finance.system.project.domain.account.dto;

import finance.system.project.domain.account.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        AccountType type,
        BigDecimal balance,
        BigDecimal initialBalance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}