package finance.system.project.domain.transaction.dto;

import finance.system.project.domain.transaction.RecurrenceType;
import finance.system.project.domain.transaction.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequest(

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type,

        @NotNull(message = "Date is required")
        LocalDate transactionDate,

        @NotNull(message = "Account is required")
        UUID accountId,

        UUID destinationAccountId, // só para TRANSFER

        UUID categoryId,

        String notes,

        RecurrenceType recurrence,

        Boolean isPaid
) {}