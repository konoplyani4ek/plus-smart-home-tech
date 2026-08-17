package ru.yandex.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.sensor.*;

public class AvroSensorEventMapper {
    private AvroSensorEventMapper() {

    }

    public static SensorEventAvro sensorEventToAvro(BaseSensorEvent sensorEvent) {
        if (sensorEvent == null) {
            throw new IllegalArgumentException("Sensor event can't be null");
        }

        SensorEventAvro sensorEventAvro = new SensorEventAvro();

        sensorEventAvro.setHubId(sensorEvent.getHubId());
        sensorEventAvro.setId(sensorEvent.getId());
        sensorEventAvro.setTimestamp(sensorEvent.getTimestamp());

        SpecificRecordBase payload;
        if (sensorEvent.getClass() == ClimateSensorEvent.class) {
            payload = toClimateSensorAvro((ClimateSensorEvent) sensorEvent);
        } else if (sensorEvent.getClass() == LightSensorEvent.class) {
            payload = toLightSensorAvro((LightSensorEvent) sensorEvent);
        } else if (sensorEvent.getClass() == MotionSensorEvent.class) {
            payload = toMotionSensorAvro((MotionSensorEvent) sensorEvent);
        } else if (sensorEvent.getClass() == SwitchSensorEvent.class) {
            payload = toSwitchSensorAvro((SwitchSensorEvent) sensorEvent);
        } else if (sensorEvent.getClass() == TemperatureSensorEvent.class) {
            payload = toTemperatureSensorAvro((TemperatureSensorEvent) sensorEvent);
        } else {
            throw new IllegalArgumentException(String.format("Unknown sensor event type: %s", sensorEvent.getClass()));
        }

        sensorEventAvro.setPayload(payload);

        return sensorEventAvro;
    }

    private static ClimateSensorAvro toClimateSensorAvro(ClimateSensorEvent obj) {
        ClimateSensorAvro avroObj = new ClimateSensorAvro();

        avroObj.setCo2Level(obj.getCo2Level());
        avroObj.setHumidity(obj.getHumidity());
        avroObj.setTemperatureC(obj.getTemperatureC());

        return avroObj;
    }

    private static LightSensorAvro toLightSensorAvro(LightSensorEvent obj) {
        LightSensorAvro avroObj = new LightSensorAvro();

        avroObj.setLinkQuality(obj.getLinkQuality());
        avroObj.setLuminosity(obj.getLuminosity());

        return avroObj;
    }

    private static MotionSensorAvro toMotionSensorAvro(MotionSensorEvent obj) {
        MotionSensorAvro avroObj = new MotionSensorAvro();

        avroObj.setLinkQuality(obj.getLinkQuality());
        avroObj.setMotion(obj.getMotion());
        avroObj.setVoltage(obj.getVoltage());

        return avroObj;
    }

    private static SwitchSensorAvro toSwitchSensorAvro(SwitchSensorEvent obj) {
        SwitchSensorAvro avroObj = new SwitchSensorAvro();

        avroObj.setState(obj.getState());

        return avroObj;
    }

    private static TemperatureSensorAvro toTemperatureSensorAvro(TemperatureSensorEvent obj) {
        TemperatureSensorAvro avroObj = new TemperatureSensorAvro();

        avroObj.setId(obj.getId());
        avroObj.setHubId(obj.getHubId());
        avroObj.setTimestamp(obj.getTimestamp());
        avroObj.setTemperatureC(obj.getTemperatureC());
        avroObj.setTemperatureF(obj.getTemperatureF());

        return avroObj;
    }

}