package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.InventoryRecord;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.mapper.InventoryMapper;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public InventoryDto getByProductId(Long productId) {
        return InventoryMapper.toDto(findEntity(productId));
    }

    @Transactional
    public InventoryDto create(UpdateInventoryRequest request) {
        InventoryRecord record = inventoryRepository.findByProductId(request.productId())
                .orElseGet(InventoryRecord::new);
        record.setProductId(request.productId());
        record.setQuantity(request.quantity());
        return InventoryMapper.toDto(inventoryRepository.save(record));
    }

    @Transactional
    public InventoryDto updateQuantity(UpdateInventoryRequest request) {
        InventoryRecord record = findEntity(request.productId());
        record.setQuantity(request.quantity());
        return InventoryMapper.toDto(inventoryRepository.save(record));
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        InventoryRecord record = findEntity(request.productId());

        if (record.getAvailableQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    "Недостаточно товара на складе для productId=" + request.productId()
                        + ". Доступно: " + record.getAvailableQuantity()
                        + ", запрошено: " + request.quantity());
        }

        record.setReservedQuantity(record.getReservedQuantity() + request.quantity());
        inventoryRepository.save(record);

        return new ReserveResponse(true, record.getAvailableQuantity(), "Резервирование выполнено успешно");
    }

    private InventoryRecord findEntity(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Складская запись для productId=" + productId + " не найдена"));
    }
}