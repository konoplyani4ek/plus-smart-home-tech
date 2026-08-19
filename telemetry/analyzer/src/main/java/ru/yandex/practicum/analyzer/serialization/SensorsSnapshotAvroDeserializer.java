package ru.yandex.practicum.analyzer.serialization;

import ru.yandex.practicum.deserializer.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public class SensorsSnapshotAvroDeserializer extends GeneralAvroDeserializer<SensorsSnapshotAvro> {

    public SensorsSnapshotAvroDeserializer() {
        super(SensorsSnapshotAvro.getClassSchema());
    }

}