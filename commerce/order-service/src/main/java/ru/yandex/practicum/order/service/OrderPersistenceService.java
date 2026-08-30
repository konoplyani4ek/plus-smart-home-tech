package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.feign.ProductClientDto;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Map;


@Service
@RequiredArgsConstructor
class OrderPersistenceService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order save(CreateOrderRequest request, Map<Long, ProductClientDto> productById) {
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.CONFIRMED);

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            ProductClientDto product = productById.get(itemRequest.productId());

            OrderItem item = new OrderItem();
            item.setProductId(product.id());
            item.setProductName(product.name());
            item.setQuantity(itemRequest.quantity());
            item.setPrice(product.price());
            order.addItem(item);

            totalPrice = totalPrice.add(product.price().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }
}