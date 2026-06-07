package finance.system.project.domain.goal;

import finance.system.project.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findByIdAndUser(UUID id, UserEntity user);
    List<GoalEntity> findAllByUser(UserEntity user);
}