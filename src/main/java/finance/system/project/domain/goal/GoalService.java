package finance.system.project.domain.goal;

import finance.system.project.domain.goal.dto.*;
import finance.system.project.domain.user.UserEntity;
import finance.system.project.domain.user.UserService;
import finance.system.project.exeception.BusinessException;
import finance.system.project.exeception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserService userService;

    @Transactional
    public GoalResponse create(String email, GoalRequest request) {
        UserEntity user = userService.findUser(email);
        GoalEntity goal = GoalEntity.builder()
                .title(request.title())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.targetDate())
                .status(GoalStatus.IN_PROGRESS)
                .color(request.color())
                .icon(request.icon())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
        return toResponse(goalRepository.save(goal));
    }

    public List<GoalResponse> listAll(String email) {
        UserEntity user = userService.findUser(email);
        return goalRepository.findAllByUser(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse getById(String email, UUID goalId) {
        return toResponse(findGoalOfUser(goalId, userService.findUser(email)));
    }

    @Transactional
    public GoalResponse update(String email, UUID goalId, GoalRequest request) {
        GoalEntity goal = findGoalOfUser(goalId, userService.findUser(email));
        goal.setTitle(request.title());
        goal.setDescription(request.description());
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());
        goal.setColor(request.color());
        goal.setIcon(request.icon());

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public void delete(String email, UUID goalId) {
        GoalEntity goal = findGoalOfUser(goalId, userService.findUser(email));
        goalRepository.delete(goal);
    }

    @Transactional
    public GoalResponse deposit(String email, UUID id, GoalDepositRequest request) {
        GoalEntity goal = findGoalOfUser(id, userService.findUser(email));

        if (goal.getStatus() == GoalStatus.ACHIEVED) {
            throw new BusinessException("Goal is already achieved");
        }

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.amount()));

        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.ACHIEVED);
        }

        return toResponse(goalRepository.save(goal));
    }


    private GoalEntity findGoalOfUser(UUID goalId, UserEntity user) {
        return goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
    }

    private GoalResponse toResponse(GoalEntity g) {
        BigDecimal remaining = g.getTargetAmount().subtract(g.getCurrentAmount());
        double pct = g.getTargetAmount().compareTo(BigDecimal.ZERO) == 0 ? 0 :
                g.getCurrentAmount()
                        .divide(g.getTargetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        return new GoalResponse(
                g.getId(),
                g.getTitle(),
                g.getDescription(),
                g.getTargetAmount(),
                g.getCurrentAmount(),
                remaining.max(BigDecimal.ZERO),
                Math.min(pct, 100.0),
                g.getTargetDate(),
                g.getStatus(),
                g.getColor(),
                g.getIcon(),
                g.getUser().getId(),
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }
}