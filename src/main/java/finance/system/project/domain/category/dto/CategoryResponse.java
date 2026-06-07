package finance.system.project.domain.category.dto;

import finance.system.project.domain.category.CategoryType;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        CategoryType type,
        String color,
        String icon,
        boolean isDefault
) {}