package ru.yandex.practicum.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(
        @NotBlank
        String server,
        @Validated
        Producer producer,
        @Validated
        Consumer consumer
) {
        public record Producer(
                @NotBlank
                String topic
        ) {
        }

        public record Consumer(
                @NotBlank
                String topic,
                @NotBlank
                String clientId,
                @NotBlank
                String groupId,
                @NotNull
                Long consumeAttemptTimeoutMs
        ) {
        }
}