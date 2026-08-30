package ru.yandex.practicum.order.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;


@Configuration
public class FeignClientConfig {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String SOURCE_SERVICE_HEADER = "X-Source-Service";
    private static final String SOURCE_SERVICE_NAME = "order-service";

    // Добавляет технические заголовки к каждому исходящему Feign-запросу:

    @Bean
    public RequestInterceptor requestContextInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(SOURCE_SERVICE_HEADER, SOURCE_SERVICE_NAME);
            requestTemplate.header(REQUEST_ID_HEADER, resolveRequestId());
        };
    }

    private String resolveRequestId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String incomingRequestId = attributes.getRequest().getHeader(REQUEST_ID_HEADER);
            if (incomingRequestId != null && !incomingRequestId.isBlank()) {
                return incomingRequestId;
            }
        }

        return UUID.randomUUID().toString();
    }
}