package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public List<ProductDto> getAllActive() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    public ProductDto getById(Long id) {
        return ProductMapper.toDto(findEntity(id));
    }

    public List<ProductDto> getByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    public List<ProductDto> search(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query).stream()
                .map(ProductMapper::toDto)
                .toList();
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setImageUrl(request.imageUrl());
        product.setActive(true);

        if (request.categoryId() != null) {
            Category category = categoryService.findEntity(request.categoryId());
            product.setCategory(category);
        }

        return ProductMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto patch(Long id, UpdateProductRequest request) {
        Product product = findEntity(id);

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.categoryId() != null) {
            product.setCategory(categoryService.findEntity(request.categoryId()));
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }

        return ProductMapper.toDto(productRepository.save(product));
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Товар с id=" + id + " не найден"));
    }
}