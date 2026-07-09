package com.enterprise.marketplace.categoryservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enterprise.marketplace.categoryservice.application.dto.CreateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.dto.UpdateCategoryRequest;
import com.enterprise.marketplace.categoryservice.application.mapper.CategoryMapper;
import com.enterprise.marketplace.categoryservice.domain.model.Category;
import com.enterprise.marketplace.categoryservice.domain.model.CategoryStatus;
import com.enterprise.marketplace.categoryservice.domain.port.CategoryRepository;
import com.enterprise.marketplace.common.exception.ErrorCode;
import com.enterprise.marketplace.common.exception.MarketplaceException;
import com.enterprise.marketplace.common.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @InjectMocks
    private CategoryApplicationService categoryApplicationService;

    @Test
    void shouldCreateCategoryWhenSlugIsUnique() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .slug("industrial-tools")
                .name("Industrial Tools")
                .displayOrder(1)
                .build();

        when(categoryRepository.existsBySlug("industrial-tools")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return category.toBuilder().id(UUID.randomUUID()).version(0L).build();
        });

        var response = categoryApplicationService.createCategory(request);

        assertThat(response.getSlug()).isEqualTo("industrial-tools");
        assertThat(response.getStatus()).isEqualTo(CategoryStatus.ACTIVE);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldThrowConflictWhenSlugAlreadyExists() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .slug("duplicate-slug")
                .name("Duplicate Category")
                .displayOrder(0)
                .build();

        when(categoryRepository.existsBySlug("duplicate-slug")).thenReturn(true);

        assertThatThrownBy(() -> categoryApplicationService.createCategory(request))
                .isInstanceOf(MarketplaceException.class)
                .extracting(ex -> ((MarketplaceException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void shouldThrowNotFoundWhenCategoryDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryApplicationService.getCategoryById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateCategoryWhenValid() {
        UUID id = UUID.randomUUID();
        Category existing = Category.builder()
                .id(id)
                .slug("old-slug")
                .name("Old Name")
                .displayOrder(0)
                .status(CategoryStatus.ACTIVE)
                .version(1L)
                .build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsBySlugAndIdNot("new-slug", id)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .slug("new-slug")
                .name("Updated Name")
                .description("Updated Description")
                .displayOrder(3)
                .status(CategoryStatus.INACTIVE)
                .build();

        var response = categoryApplicationService.updateCategory(id, request);

        assertThat(response.getSlug()).isEqualTo("new-slug");
        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getStatus()).isEqualTo(CategoryStatus.INACTIVE);
    }
}
