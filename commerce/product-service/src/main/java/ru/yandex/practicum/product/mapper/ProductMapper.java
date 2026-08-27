// commerce/product-service/src/main/java/ru/yandex/practicum/product/mapper/ProductMapper.java
package ru.yandex.practicum.product.mapper;

import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDto toDto(Product product) {
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