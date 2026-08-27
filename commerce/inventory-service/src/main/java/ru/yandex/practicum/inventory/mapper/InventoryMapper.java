package ru.yandex.practicum.inventory.mapper;

import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.entity.InventoryRecord;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static InventoryDto toDto(InventoryRecord record) {
        return new InventoryDto(
                record.getId(),
                record.getProductId(),
                record.getQuantity(),
                record.getReservedQuantity(),
                record.getAvailableQuantity()
        );
    }
}