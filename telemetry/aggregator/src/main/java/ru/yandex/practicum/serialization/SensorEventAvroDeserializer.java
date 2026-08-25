package ru.yandex.practicum.serialization;


import ru.yandex.practicum.deserializer.GeneralAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

public class SensorEventAvroDeserializer extends GeneralAvroDeserializer<SensorEventAvro> {

    public SensorEventAvroDeserializer() {
        super(SensorEventAvro.getClassSchema());
    }

}