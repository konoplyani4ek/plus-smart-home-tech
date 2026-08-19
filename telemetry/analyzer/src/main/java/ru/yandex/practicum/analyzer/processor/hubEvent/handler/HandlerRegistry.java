package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.util.HashMap;
import java.util.Map;

@Component
public class HandlerRegistry {
    private final Map<Class<? extends SpecificRecordBase>, HubEventHandler> handlers;

    public HandlerRegistry(DeviceAddedEventHandler deviceAddedEventHandler,
                           DeviceRemovedEventHandler deviceRemovedEventHandler,
                           ScenarioAddedEventHandler scenarioAddedEventHandler,
                           ScenarioRemovedEventHandler scenarioRemovedEventHandler) {

        handlers = new HashMap<>();
        handlers.put(DeviceAddedEventAvro.class, deviceAddedEventHandler);
        handlers.put(DeviceRemovedEventAvro.class, deviceRemovedEventHandler);
        handlers.put(ScenarioAddedEventAvro.class, scenarioAddedEventHandler);
        handlers.put(ScenarioRemovedEventAvro.class, scenarioRemovedEventHandler);
    }

    public HubEventHandler getHandler(Class<? extends SpecificRecordBase> type) {
        return handlers.get(type);
    }

}