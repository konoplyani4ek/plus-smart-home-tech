package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .toList();
    }

    public CategoryDto getById(Long id) {
        return CategoryMapper.toDto(findEntity(id));
    }

    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория с id=" + id + " не найдена"));
    }
}