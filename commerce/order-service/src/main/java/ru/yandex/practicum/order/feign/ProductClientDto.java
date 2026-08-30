package ru.yandex.practicum.order.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
// чтобы order-service не ломался, если product-service вернёт дополнительные поля (description, category, imageUrl и т.д.).
public record ProductClientDto(

        Long id,

        String name,

        BigDecimal price,

        Boolean active
) {
}