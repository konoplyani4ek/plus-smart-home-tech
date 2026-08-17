package ru.yandex.practicum.service;

import ru.yandex.practicum.model.hub.BaseHubEvent;
import ru.yandex.practicum.model.sensor.BaseSensorEvent;

public interface EventService {
    void sendSensorEvent(BaseSensorEvent sensorEvent);

    void sendHubEvent(BaseHubEvent hubEvent);
}