package finance.system.project.domain.transaction.dto;

import finance.system.project.domain.transaction.RecurrenceType;
import finance.system.project.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String description,
        BigDecimal amount,
        TransactionType type,
        LocalDate transactionDate,
        UUID accountId,
        String accountName,
        UUID destinationAccountId,
        String destinationAccountName,
        UUID categoryId,
        String categoryName,
        String notes,
        RecurrenceType recurrence,
        Boolean isPaid,
        LocalDateTime createdAt
) {}