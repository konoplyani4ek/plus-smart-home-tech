package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;


@Component
public class DeviceRemovedEventHandler implements HubEventHandler<DeviceRemovedEventAvro>{
    private final Logger log = LoggerFactory.getLogger(DeviceRemovedEventHandler.class);
    private final SensorRepository sensorRepository;

    public DeviceRemovedEventHandler(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    @Transactional
    public void handle(String hubId, DeviceRemovedEventAvro payload) {
        String sensorId = payload.getId();

        sensorRepository.deleteByIdAndHubId(sensorId, hubId);
        log.info("delete sensor id={}, hubId={}", sensorId, hubId);
    }
}