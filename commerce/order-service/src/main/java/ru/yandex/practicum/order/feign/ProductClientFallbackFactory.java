package ru.yandex.practicum.order.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.exception.ProductServiceUnavailableException;

@Component
@Slf4j
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        return productId -> {
            if (isBusinessResponse(cause)) {
                log.debug("product-service ответил бизнес-ошибкой для productId={}: {}",
                        productId, cause.getMessage());
                throw (FeignException) cause;
            }
            log.warn("product-service технически недоступен для productId={}: {}",
                    productId, cause.getMessage());
            throw new ProductServiceUnavailableException(productId, cause);
        };
    }

    private boolean isBusinessResponse(Throwable cause) {
        return cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500;
    }
}