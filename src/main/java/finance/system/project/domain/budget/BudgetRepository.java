package finance.system.project.domain.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
}