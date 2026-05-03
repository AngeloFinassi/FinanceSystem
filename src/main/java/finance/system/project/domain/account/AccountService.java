package finance.system.project.domain.account;

import finance.system.project.domain.account.dto.AccountRequest;
import finance.system.project.domain.account.dto.AccountResponse;
import finance.system.project.domain.user.UserEntity;
import finance.system.project.domain.user.UserRepository;
import finance.system.project.domain.user.UserService;
import finance.system.project.domain.user.dto.UserProfileResponse;
import finance.system.project.exeception.BusinessException;
import finance.system.project.exeception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse create(String email, AccountRequest request) {
        UserEntity user = findUser(email);

        if (accountRepository.existsByNameAndUser(request.name(), user)) {
            throw new BusinessException("Account with the same name already exists for this user");
        }

        AccountEntity account = AccountEntity.builder()
                .name(request.name())
                .type(request.accountType())
                .initialBalance(request.initialBalance())
                .balance(request.initialBalance())
                .user(user)
                .build();

        return toResponse(accountRepository.save(account));
    }

    public List<AccountResponse> listAll(String email) {
        UserEntity user = findUser(email);
        return accountRepository.findAllByUser(user).stream()
                .map(this::toResponse)
                .toList();
    }

    public AccountResponse getById(String email, UUID accountId){
        return toResponse(findAccountOwnedByUser(email, accountId));
    }

    @Transactional
    public AccountResponse update(String email, UUID accountId, AccountRequest request) {
        AccountEntity account = findAccountOwnedByUser(email, accountId);

        if (!account.getName().equals(request.name()) && accountRepository.existsByNameAndUser(request.name(), account.getUser())) {
            throw new BusinessException("Account with the same name already exists for this user");
        }

        account.setName(request.name());
        account.setType(request.accountType());
        account.setInitialBalance(request.initialBalance());
        // Note: Balance should not be updated directly here, it should be managed through transactions

        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public void delete(String emai, UUID accountId) {
        AccountEntity account = findAccountOwnedByUser(emai, accountId);
        accountRepository.delete(account);
    }

    @Transactional
    public AccountResponse credit(UUID accountId, BigDecimal amount) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse debit(UUID accountId, BigDecimal amount) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(account.getBalance().subtract(amount));
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    private AccountEntity findAccountOwnedByUser(String email, UUID accountId) {
        UserEntity user = findUser(email);
        return accountRepository.findByIdAndUser(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private UserEntity findUser(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AccountResponse toResponse(AccountEntity a){
        return new AccountResponse(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getBalance(),
                a.getInitialBalance(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
