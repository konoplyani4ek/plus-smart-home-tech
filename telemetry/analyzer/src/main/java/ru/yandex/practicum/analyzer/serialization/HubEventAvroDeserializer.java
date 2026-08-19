package ru.yandex.practicum.analyzer.serialization;


import ru.yandex.practicum.deserializer.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public class HubEventAvroDeserializer extends GeneralAvroDeserializer<HubEventAvro> {

    public HubEventAvroDeserializer() {
        super(HubEventAvro.getClassSchema());
    }

}