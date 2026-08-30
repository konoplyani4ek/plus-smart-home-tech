package ru.yandex.practicum.order.feign;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReleaseRequest(

        @NotNull
        Long productId,

        @NotNull
        @Min(1)
        Integer quantity
) {
}