package finance.system.project.domain.category.dto;

import finance.system.project.domain.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Category type is required")
        CategoryType type, // INCOME or EXPENSE

        String color,
        String icon
) {}
