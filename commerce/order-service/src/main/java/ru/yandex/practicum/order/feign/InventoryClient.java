package ru.yandex.practicum.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Клиент к inventory-service. Имя "inventory-service" используется Eureka для discovery
 * и как ключ отдельных Feign-настроек (feign.client.config.inventory-service.*) во внешней конфигурации.
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/inventory/reserve")
    ReserveResponse reserve(@RequestBody ReserveRequest request);

    @PostMapping("/api/inventory/release")
    ReserveResponse release(@RequestBody ReleaseRequest request);
}