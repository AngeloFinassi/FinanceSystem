package finance.system.project.domain.account;

import finance.system.project.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByIdAndUser(UUID id, UserEntity user);
    boolean existsByNameAndUser(String name, UserEntity user);
    List<AccountEntity> findAllByUser(UserEntity user);
}
