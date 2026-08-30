package ru.yandex.practicum.order.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.*;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderPersistenceService orderPersistenceService;

    public OrderDto create(CreateOrderRequest request) {
        // 1. Суммарное количество по каждому productId — для одного резерва на суммарное
        //    количество, даже если товар встречается в заказе несколько раз.
        Map<Long, Integer> quantityByProductId = aggregateQuantities(request);

        // 2. Данные товара запрашиваются один раз на каждый уникальный productId
        //    и переиспользуются для всех позиций заказа с этим productId.
        Map<Long, ProductClientDto> productById = fetchAndValidateProducts(quantityByProductId.keySet());

        // 3. Резервирование остатков на складе. Успешные резервы запоминаем,
        //    чтобы иметь возможность их компенсировать при ошибке дальше по сценарию.
        List<Long> reservedProductIds = new ArrayList<>();
        try {
            reserveStock(quantityByProductId, reservedProductIds);
        } catch (RuntimeException e) {
            compensateReservations(reservedProductIds, quantityByProductId);
            throw e;
        }

        // 4. Сохранение заказа со снимком товарных данных — в отдельной DB-транзакции,
        //    без каких-либо сетевых вызовов внутри неё.
        try {
            Order savedOrder = orderPersistenceService.save(request, productById);
            return OrderMapper.toDto(savedOrder);
        } catch (RuntimeException e) {
            log.error("Не удалось сохранить заказ после успешного резервирования, выполняется компенсация", e);
            compensateReservations(reservedProductIds, quantityByProductId);
            throw new OrderProcessingException("Не удалось оформить заказ, попробуйте повторить позже", e);
        }
    }

    public OrderDto getById(Long id) {
        return OrderMapper.toDto(findEntity(id));
    }

    public List<OrderDto> getAll() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    public List<OrderDto> getByEmail(String email) {
        return orderRepository.findByCustomerEmail(email).stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    private Order findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Заказ с id=" + id + " не найден"));
    }

    private Map<Long, Integer> aggregateQuantities(CreateOrderRequest request) {
        Map<Long, Integer> quantityByProductId = new LinkedHashMap<>();
        for (OrderItemRequest item : request.items()) {
            quantityByProductId.merge(item.productId(), item.quantity(), Integer::sum);
        }
        return quantityByProductId;
    }

    private Map<Long, ProductClientDto> fetchAndValidateProducts(Iterable<Long> productIds) {
        Map<Long, ProductClientDto> productById = new LinkedHashMap<>();
        for (Long productId : productIds) {
            ProductClientDto product = fetchProduct(productId);
            if (Boolean.FALSE.equals(product.active())) {
                throw new OrderProcessingException(
                        "Товар '" + product.name() + "' (id=" + productId + ") снят с продажи");
            }
            productById.put(productId, product);
        }
        return productById;
    }

    private ProductClientDto fetchProduct(Long productId) {
        try {
            return productClient.getById(productId);
        } catch (FeignException.NotFound e) {
            throw new OrderProcessingException("Товар с id=" + productId + " не найден в каталоге");
        } catch (FeignException e) {
            throw new OrderProcessingException(
                    "Не удалось получить данные о товаре id=" + productId + " из каталога", e);
        }
    }

    private void reserveStock(Map<Long, Integer> quantityByProductId, List<Long> reservedProductIds) {
        for (Map.Entry<Long, Integer> entry : quantityByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            try {
                inventoryClient.reserve(new ReserveRequest(productId, quantity));
                reservedProductIds.add(productId);
            } catch (FeignException.NotFound e) {
                throw new OrderProcessingException(
                        "Складская запись для товара id=" + productId + " не найдена");
            } catch (FeignException.Conflict e) {
                throw new OrderProcessingException(
                        "Недостаточно товара на складе для id=" + productId);
            } catch (FeignException e) {
                throw new OrderProcessingException(
                        "Не удалось зарезервировать товар id=" + productId + " на складе", e);
            }
        }
    }

    /**
     * Снимает уже созданные резервы для тех productId, по которым резервирование
     * прошло успешно, если сценарий заказа сорвался позже. Ошибка самой компенсации
     * логируется, но не подменяет исходную ошибку сценария заказа.
     */
    private void compensateReservations(List<Long> reservedProductIds, Map<Long, Integer> quantityByProductId) {
        for (Long productId : reservedProductIds) {
            Integer quantity = quantityByProductId.get(productId);
            try {
                inventoryClient.release(new ReleaseRequest(productId, quantity));
                log.info("Резерв снят по компенсации: productId={}, quantity={}", productId, quantity);
            } catch (FeignException e) {
                log.error("Не удалось снять резерв по компенсации для productId={}, quantity={}: {}",
                        productId, quantity, e.getMessage());
            }
        }
    }
}