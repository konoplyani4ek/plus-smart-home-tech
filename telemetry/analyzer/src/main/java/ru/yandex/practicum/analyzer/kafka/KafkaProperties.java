package ru.yandex.practicum.analyzer.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka")
@Validated
public record KafkaProperties(
        @NotBlank
        String server,
        Long retryPeriodMs,
        @Valid
        Consumer consumer
) {
        public record Consumer(
                @Valid
                SnapshotProcessor snapshotProcessor,
                @Valid
                HubEventProcessor hubEventProcessor,
                @NotNull
                @Positive
                long pollTimeout
        ) {
        }

        public record SnapshotProcessor(
                @NotBlank
                String topic,
                @NotBlank
                String clientId,
                @NotBlank
                String groupId
        ) {
        }

        public record HubEventProcessor(
                @NotBlank
                String topic,
                @NotBlank
                String clientId,
                @NotBlank
                String groupId
        ) {
        }
}