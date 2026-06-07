package finance.system.project.domain.budget;

import finance.system.project.domain.category.CategoryEntity;
import finance.system.project.domain.category.CategoryRepository;
import finance.system.project.domain.transaction.TransactionRepository;
import finance.system.project.domain.transaction.TransactionType;
import finance.system.project.domain.budget.dto.*;
import finance.system.project.domain.user.UserEntity;
import finance.system.project.domain.user.UserRepository;
import finance.system.project.domain.user.UserService;
import finance.system.project.exeception.BusinessException;
import finance.system.project.exeception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public BudgetResponse create(String email, BudgetRequest request) {
        UserEntity user = userService.findUser(email);
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        boolean alreadyExists = budgetRepository
                .existsByUserAndCategoryAndMonthAndYear(user, category, request.month(), request.year());
        if (alreadyExists) {
            throw new BusinessException("Budget for this category/month/year already exists");
        }

        BudgetEntity budget = BudgetEntity.builder()
                .user(user)
                .category(category)
                .limitAmount(request.limitAmount())
                .month(request.month())
                .year(request.year())
                .build();

        return toResponse(budgetRepository.save(budget), user);
    }

    public List<BudgetResponse> listByMonthYear(String email, Integer month, Integer year) {
        UserEntity user = userService.findUser(email);
        return budgetRepository.findAllByUserAndMonthAndYear(user, month, year)
                .stream()
                .map(b -> toResponse(b, user))
                .toList();
    }

    public BudgetResponse getById(String email, UUID id) {
        UserEntity user = userService.findUser(email);
        return toResponse(findBudgetOfUser(id, user), user);
    }

    @Transactional
    public BudgetResponse update(String email, UUID id, BudgetRequest request) {
        UserEntity user = userService.findUser(email);
        BudgetEntity budget = findBudgetOfUser(id, user);
        budget.setLimitAmount(request.limitAmount());
        return toResponse(budgetRepository.save(budget), user);
    }

    @Transactional
    public void delete(String email, UUID id) {
        UserEntity user = userService.findUser(email);
        budgetRepository.delete(findBudgetOfUser(id, user));
    }

    private BigDecimal calculateSpent(UserEntity user, CategoryEntity category, int month, int year) {
        return transactionRepository
                .sumByUserAndCategoryAndTypeAndMonthAndYear(
                        user, category, TransactionType.EXPENSE, month, year)
                .orElse(BigDecimal.ZERO);
    }

    private BudgetEntity findBudgetOfUser(UUID id, UserEntity user) {
        return budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
    }

    private BudgetResponse toResponse(BudgetEntity b, UserEntity user) {
        BigDecimal spent = calculateSpent(user, b.getCategory(), b.getMonth(), b.getYear());
        BigDecimal remaining = b.getLimitAmount().subtract(spent);
        double pct = b.getLimitAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                spent.divide(b.getLimitAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        return new BudgetResponse(
                b.getId(),
                b.getCategory().getId(),
                b.getCategory().getName(),
                b.getLimitAmount(),
                spent,
                remaining,
                pct,
                b.getMonth(),
                b.getYear()
        );
    }
}