package finance.system.project.domain.category;


import finance.system.project.domain.category.dto.CategoryRequest;
import finance.system.project.domain.category.dto.CategoryResponse;
import finance.system.project.domain.user.UserEntity;
import finance.system.project.domain.user.UserRepository;
import finance.system.project.domain.user.UserService;
import finance.system.project.exeception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public CategoryResponse create(String email, CategoryRequest request){
        UserEntity user = userService.findUser(email);

        if (categoryRepository.existsByNameAndUser(request.name(), user)) {
            throw new BusinessException("Category with this name already exists");
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.name())
                .type(request.type())
                .color(request.color())
                .isDefault(false) //cateogry create by users are not default
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> listAll(String email){
        UserEntity user = userService.findUser(email);

        return categoryRepository.findAllByUserOrIsDefaultTrue(user)
                .stream() //turn into elements flux List<CategoryEntity> -> Stream<CategoryEntity>
                .map(this::toResponse) //turn each element of the stream into a CategoryResponse using the toResponse method
                //foodEntity -> foodResponse, so turn Strem<FoodResponse> -> List<FoodResponse>
                .toList();
    }

    public CategoryResponse getById(String email, UUID categoryId){
        return toResponse(findCategoryAcessibleByUser(email, categoryId));
    }

    @Transactional
    public CategoryResponse update(String email, UUID categoryId, CategoryRequest request){
        CategoryEntity category = findCategoryAcessibleByUser(email, categoryId);

        if (category.getIsDefault()) {
            throw new BusinessException("Default categories cannot be updated");
        }

        if (!category.getName().equals(request.name()) && categoryRepository.existsByNameAndUser(request.name(), category.getUser())) {
            throw new BusinessException("Category with this name already exists");
        }

        category.setName(request.name());
        category.setType(request.type());
        category.setColor(request.color());
        category.setIcon(request.icon());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(String email, UUID categoryId){
        CategoryEntity category = findCategoryAcessibleByUser(email, categoryId);

        if (category.getIsDefault()) {
            throw new BusinessException("Default categories cannot be deleted");
        }

        categoryRepository.delete(category);
    }

    //find a category by id, but only if it belongs to the user or is a default category
    private CategoryEntity findCategoryAcessibleByUser(String email, UUID cateogryId) {
        UserEntity user = userService.findUser(email);
        return categoryRepository.findByIdAndUserOrIsDefaultTrue(cateogryId, user)
                .orElseThrow(() -> new BusinessException("Category not found"));
    }

    private CategoryResponse toResponse(CategoryEntity c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getType(),
                c.getColor(),
                c.getIcon(),
                c.getIsDefault()
        );
    }

}
