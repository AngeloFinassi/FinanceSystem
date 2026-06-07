package finance.system.project.domain.transaction.dto;

import finance.system.project.domain.transaction.TransactionType;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionFilterRequest(
        TransactionType type,
        UUID accountId,
        UUID categoryId,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isPaid
) {}