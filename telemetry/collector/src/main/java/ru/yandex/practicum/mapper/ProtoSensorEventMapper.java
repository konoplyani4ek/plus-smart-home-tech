package ru.yandex.practicum.mapper;



import com.google.protobuf.Timestamp;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.*;

import java.time.Instant;



public final class ProtoSensorEventMapper {

    private ProtoSensorEventMapper() {
    }

    public static BaseSensorEvent toModel(SensorEventProto proto) {
        if (proto == null) {
            throw new IllegalArgumentException("Sensor event proto can't be null");
        }

        BaseSensorEvent event;

        switch (proto.getPayloadCase()) {
            case MOTION_SENSOR:
                MotionSensorEvent motionEvent = new MotionSensorEvent();
                motionEvent.setLinkQuality(proto.getMotionSensor().getLinkQuality());
                motionEvent.setMotion(proto.getMotionSensor().getMotion());
                motionEvent.setVoltage(proto.getMotionSensor().getVoltage());
                event = motionEvent;
                break;

            case TEMPERATURE_SENSOR:
                TemperatureSensorEvent temperatureEvent = new TemperatureSensorEvent();
                temperatureEvent.setTemperatureC(proto.getTemperatureSensor().getTemperatureC());
                temperatureEvent.setTemperatureF(proto.getTemperatureSensor().getTemperatureF());
                event = temperatureEvent;
                break;

            case LIGHT_SENSOR:
                LightSensorEvent lightEvent = new LightSensorEvent();
                lightEvent.setLinkQuality(proto.getLightSensor().getLinkQuality());
                lightEvent.setLuminosity(proto.getLightSensor().getLuminosity());
                event = lightEvent;
                break;

            case CLIMATE_SENSOR:
                ClimateSensorEvent climateEvent = new ClimateSensorEvent();
                climateEvent.setTemperatureC(proto.getClimateSensor().getTemperatureC());
                climateEvent.setHumidity(proto.getClimateSensor().getHumidity());
                climateEvent.setCo2Level(proto.getClimateSensor().getCo2Level());
                event = climateEvent;
                break;

            case SWITCH_SENSOR:
                SwitchSensorEvent switchEvent = new SwitchSensorEvent();
                switchEvent.setState(proto.getSwitchSensor().getState());
                event = switchEvent;
                break;

            case PAYLOAD_NOT_SET:
            default:
                throw new IllegalArgumentException("Sensor event payload is not set");
        }

        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        if (proto.hasTimestamp()) {
            event.setTimestamp(toInstant(proto.getTimestamp()));
        }

        return event;
    }

    private static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}