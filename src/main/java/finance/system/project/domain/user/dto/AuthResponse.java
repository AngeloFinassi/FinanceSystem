package finance.system.project.domain.user.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String name,
        String email,
        String role
) {}