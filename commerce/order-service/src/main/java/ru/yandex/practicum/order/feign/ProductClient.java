package ru.yandex.practicum.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Клиент к product-service. Имя "product-service" используется Eureka для discovery
 * (совпадает со spring.application.name product-service).
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductClientDto getById(@PathVariable("id") Long id);
}