package ru.yandex.practicum.product.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.entity.Category;

@UtilityClass
public class CategoryMapper {

    public CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}