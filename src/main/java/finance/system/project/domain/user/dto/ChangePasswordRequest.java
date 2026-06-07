package finance.system.project.domain.user.dto;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {
}
