package finance.system.project.domain.user.dto;

public record UpdateProfileRequest(
        String name,
        String email
) {
}
