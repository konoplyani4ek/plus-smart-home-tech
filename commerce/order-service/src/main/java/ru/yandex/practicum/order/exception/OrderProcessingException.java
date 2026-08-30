package ru.yandex.practicum.order.exception;

// ошибка оформления заказа: товар не найден, товар снят с продажи, недостаточно остатков на складе

public class OrderProcessingException extends RuntimeException {

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}