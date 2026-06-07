package finance.system.project.domain.transaction;

import finance.system.project.domain.account.AccountEntity;
import finance.system.project.domain.account.AccountRepository;
import finance.system.project.domain.category.CategoryRepository;
import finance.system.project.domain.transaction.dto.*;
import finance.system.project.domain.transaction.dto.TransactionRequest;
import finance.system.project.domain.user.UserEntity;
import finance.system.project.domain.user.UserRepository;
import finance.system.project.exeception.BusinessException;
import finance.system.project.exeception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse create(String email, TransactionRequest request) {
        UserEntity user = findUser(email);
        AccountEntity account = findAccountOfUser(request.accountId(), user);

        validateTransferRules(request, user);

        TransactionEntity transaction = TransactionEntity.builder()
                .description(request.description())
                .amount(request.amount())
                .type(request.type())
                .transactionDate(request.transactionDate())
                .account(account)
                .destinationAccount(resolveDestinationAccount(request, user))
                .category(request.categoryId() != null
                        ? categoryRepository.findById(request.categoryId()).orElse(null)
                        : null)
                .user(user)
                .notes(request.notes())
                .recurrence(request.recurrence() != null ? request.recurrence() : RecurrenceType.NONE)
                .isPaid(request.isPaid() != null ? request.isPaid() : true)
                .createdAt(LocalDateTime.now())
                .build();

        // updates the balance who recieve the transaction
        updateBalance(account, request.amount(), request.type(), false);

        // If type its trafer, so the update decrease the balance
        if (request.type() == TransactionType.TRANSFER && transaction.getDestinationAccount() != null) {
            updateBalance(transaction.getDestinationAccount(), request.amount(), TransactionType.INCOME, false);
        }

        return toResponse(transactionRepository.save(transaction));
    }

    public Page<TransactionResponse> listAll(String email, TransactionFilterRequest filter, Pageable pageable) {
        UserEntity user = findUser(email);
        return transactionRepository.findAllWithFilters(
                user,
                filter.type(),
                filter.accountId(),
                filter.categoryId(),
                filter.startDate(),
                filter.endDate(),
                filter.isPaid(),
                pageable
        ).map(this::toResponse);
    }

    public TransactionResponse getById(String email, UUID transactionId) {
        UserEntity user = findUser(email);
        return toResponse(findTransactionOfUser(transactionId, user));
    }

    @Transactional
    public TransactionResponse update(String email, UUID transactionId, TransactionRequest request) {
        UserEntity user = findUser(email);
        TransactionEntity transaction = findTransactionOfUser(transactionId, user);

        // change the balance to the old transaction before update, to avoid inconsistency
        updateBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType(), true);
        if (transaction.getType() == TransactionType.TRANSFER && transaction.getDestinationAccount() != null) {
            updateBalance(transaction.getDestinationAccount(), transaction.getAmount(), TransactionType.EXPENSE, true);
        }

        // Apply the new balance after update
        AccountEntity newAccount = findAccountOfUser(request.accountId(), user);
        updateBalance(newAccount, request.amount(), request.type(), false);
        if (request.type() == TransactionType.TRANSFER) {
            AccountEntity destination = resolveDestinationAccount(request, user);
            if (destination != null) updateBalance(destination, request.amount(), TransactionType.INCOME, false);
            transaction.setDestinationAccount(destination);
        }

        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setType(request.type());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setAccount(newAccount);
        transaction.setCategory(request.categoryId() != null
                ? categoryRepository.findById(request.categoryId()).orElse(null)
                : null);
        transaction.setNotes(request.notes());
        transaction.setIsPaid(request.isPaid() != null ? request.isPaid() : true);

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public void delete(String email, UUID transactionId) {
        UserEntity user = findUser(email);
        TransactionEntity transaction = findTransactionOfUser(transactionId, user);

        // Reverte the balance when delete
        updateBalance(transaction.getAccount(), transaction.getAmount(), transaction.getType(), true);
        if (transaction.getType() == TransactionType.TRANSFER && transaction.getDestinationAccount() != null) {
            updateBalance(transaction.getDestinationAccount(), transaction.getAmount(), TransactionType.EXPENSE, true);
        }

        transactionRepository.delete(transaction);
    }

    // if true revert the operation
    private void updateBalance(AccountEntity account, BigDecimal amount,
                               TransactionType type, boolean revert) {
        BigDecimal current = account.getBalance();
        if (type == TransactionType.INCOME) {
            account.setBalance(revert ? current.subtract(amount) : current.add(amount));
        } else if (type == TransactionType.EXPENSE) {
            account.setBalance(revert ? current.add(amount) : current.subtract(amount));
        }
        accountRepository.save(account);
    }

    private AccountEntity resolveDestinationAccount(TransactionRequest request, UserEntity user) {
        if (request.type() == TransactionType.TRANSFER && request.destinationAccountId() != null) {
            return findAccountOfUser(request.destinationAccountId(), user);
        }
        return null;
    }

    private void validateTransferRules(TransactionRequest request, UserEntity user) {
        if (request.type() == TransactionType.TRANSFER) {
            if (request.destinationAccountId() == null) {
                throw new BusinessException("Destination account is required for transfers");
            }
            if (request.accountId().equals(request.destinationAccountId())) {
                throw new BusinessException("Source and destination accounts must be different");
            }
        }
    }

    private TransactionEntity findTransactionOfUser(UUID id, UserEntity user) {
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private AccountEntity findAccountOfUser(UUID accountId, UserEntity user) {
        return accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private TransactionResponse toResponse(TransactionEntity t) {
        return new TransactionResponse(
                t.getId(),
                t.getDescription(),
                t.getAmount(),
                t.getType(),
                t.getTransactionDate(),
                t.getAccount().getId(),
                t.getAccount().getName(),
                t.getDestinationAccount() != null ? t.getDestinationAccount().getId() : null,
                t.getDestinationAccount() != null ? t.getDestinationAccount().getName() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getNotes(),
                t.getRecurrence(),
                t.getIsPaid(),
                t.getCreatedAt()
        );
    }
}