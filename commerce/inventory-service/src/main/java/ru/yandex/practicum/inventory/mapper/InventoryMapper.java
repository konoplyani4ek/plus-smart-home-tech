package ru.yandex.practicum.inventory.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.entity.InventoryRecord;

@UtilityClass
public class InventoryMapper {

    public InventoryDto toDto(InventoryRecord record) {
        return new InventoryDto(
                record.getId(),
                record.getProductId(),
                record.getQuantity(),
                record.getReservedQuantity(),
                record.getAvailableQuantity()
        );
    }
}