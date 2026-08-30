package ru.yandex.practicum.order.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.exception.InventoryServiceUnavailableException;

@Component
@Slf4j
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClient() {

            @Override
            public ReserveResponse reserve(ReserveRequest request) {
                if (isBusinessResponse(cause)) {
                    log.debug("inventory-service ответил бизнес-ошибкой при резервировании productId={}: {}",
                            request.productId(), cause.getMessage());
                    throw (FeignException) cause;
                }
                log.warn("inventory-service технически недоступен при резервировании productId={}: {}",
                        request.productId(), cause.getMessage());
                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }

            @Override
            public ReserveResponse release(ReleaseRequest request) {
                if (isBusinessResponse(cause)) {
                    log.debug("inventory-service ответил бизнес-ошибкой при снятии резерва productId={}: {}",
                            request.productId(), cause.getMessage());
                    throw (FeignException) cause;
                }
                log.warn("inventory-service технически недоступен при снятии резерва productId={}: {}",
                        request.productId(), cause.getMessage());
                throw new InventoryServiceUnavailableException(request.productId(), cause);
            }
        };
    }

    private boolean isBusinessResponse(Throwable cause) {
        return cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500;
    }
}