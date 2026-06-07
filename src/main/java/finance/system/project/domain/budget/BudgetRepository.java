package finance.system.project.domain.budget;

import finance.system.project.domain.category.CategoryEntity;
import finance.system.project.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    Optional<BudgetEntity> findByIdAndUser(UUID id, UserEntity user);
    List<BudgetEntity> findAllByUserAndMonthAndYear(UserEntity user, Integer month, Integer year);
    boolean existsByUserAndCategoryAndMonthAndYear(UserEntity user, CategoryEntity category, Integer month, Integer year);
}