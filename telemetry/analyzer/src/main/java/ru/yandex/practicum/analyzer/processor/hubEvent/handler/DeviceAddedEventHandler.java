package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.DeviceType;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;


import java.util.Optional;

@Component
public class DeviceAddedEventHandler implements HubEventHandler<DeviceAddedEventAvro>{
    private final Logger log = LoggerFactory.getLogger(DeviceAddedEventHandler.class);
    private final SensorRepository sensorRepository;

    public DeviceAddedEventHandler(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @Override
    @Transactional
    public void handle(String hubId, DeviceAddedEventAvro payload) {
        String sensorId = payload.getId();
        Optional<Sensor> searchResult = sensorRepository.findByIdAndHubId(sensorId, hubId);

        if (searchResult.isPresent()) {
            log.info("sensor id={}, hubId={} is already created", sensorId, hubId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensor.setType(DeviceType.valueOf(payload.getType().name()));
        sensorRepository.save(sensor);

        log.info("created sensor id={}, hubId={}", sensorId, hubId);
    }
}