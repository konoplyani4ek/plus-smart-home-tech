package ru.yandex.practicum.product.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.entity.Product;

@UtilityClass
public class ProductMapper {

    public ProductDto toDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                CategoryMapper.toDto(product.getCategory()),
                product.getImageUrl(),
                product.getActive()
        );
    }
}