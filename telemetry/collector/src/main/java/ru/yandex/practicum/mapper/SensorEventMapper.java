package ru.yandex.practicum.mapper;

import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;

public class SensorEventMapper {

    private SensorEventMapper() {
    }

    public static SensorEventAvro mapToAvro(SensorEvent event) {
        Object payload = switch (event.getType()) {
            case CLIMATE_SENSOR_EVENT -> mapClimate((ClimateSensorEvent) event);
            case LIGHT_SENSOR_EVENT -> mapLight((LightSensorEvent) event);
            case MOTION_SENSOR_EVENT -> mapMotion((MotionSensorEvent) event);
            case SWITCH_SENSOR_EVENT -> mapSwitch((SwitchSensorEvent) event);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperature((TemperatureSensorEvent) event);
        };

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private static ClimateSensorAvro mapClimate(ClimateSensorEvent event) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setHumidity(event.getHumidity())
                .setCo2Level(event.getCo2Level())
                .build();
    }

    private static LightSensorAvro mapLight(LightSensorEvent event) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setLuminosity(event.getLuminosity())
                .build();
    }

    private static MotionSensorAvro mapMotion(MotionSensorEvent event) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.isMotion())
                .setVoltage(event.getVoltage())
                .build();
    }

    private static SwitchSensorAvro mapSwitch(SwitchSensorEvent event) {
        return SwitchSensorAvro.newBuilder()
                .setState(event.isState())
                .build();
    }

    private static TemperatureSensorAvro mapTemperature(TemperatureSensorEvent event) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();
    }
}
