package finance.system.project.domain.goal;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
}