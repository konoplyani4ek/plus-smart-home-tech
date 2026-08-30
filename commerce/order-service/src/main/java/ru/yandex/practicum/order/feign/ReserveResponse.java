package ru.yandex.practicum.order.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Ответ inventory-service на резервирование (POST /api/inventory/reserve)
 * и на снятие резерва (POST /api/inventory/release)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReserveResponse(

        boolean success,

        Integer availableQuantity,

        String message
) {
}