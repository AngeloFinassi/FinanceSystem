package finance.system.project.domain.category;

import finance.system.project.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    boolean existsByNameAndUser(String name, UserEntity user);
    List<CategoryEntity> findAllByUserOrIsDefaultTrue(UserEntity user);
    Optional<CategoryEntity> findByIdAndUserOrIsDefaultTrue(UUID id, UserEntity user);

}
