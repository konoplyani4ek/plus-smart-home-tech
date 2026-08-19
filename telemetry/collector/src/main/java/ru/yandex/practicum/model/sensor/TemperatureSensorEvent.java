package ru.yandex.practicum.model.sensor;

import jakarta.validation.constraints.NotNull;

public class TemperatureSensorEvent extends BaseSensorEvent {

    @NotNull
    private Integer temperatureC;

    @NotNull
    private Integer temperatureF;

    public Integer getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(Integer temperatureC) {
        this.temperatureC = temperatureC;
    }

    public Integer getTemperatureF() {
        return temperatureF;
    }

    @Override
    public SensorEventType getType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    public void setTemperatureF(Integer temperatureF) {
        this.temperatureF = temperatureF;
    }
}