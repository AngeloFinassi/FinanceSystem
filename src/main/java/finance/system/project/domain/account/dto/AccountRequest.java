package finance.system.project.domain.account.dto;

import finance.system.project.domain.account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Account type is required")
        AccountType accountType,

        @NotNull(message = "Initial balance is required")
        BigDecimal initialBalance
) {}
