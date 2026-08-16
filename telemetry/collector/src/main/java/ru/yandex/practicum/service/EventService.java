package ru.yandex.practicum.service;

import ru.yandex.practicum.model.hub.BaseHubEvent;
import ru.yandex.practicum.model.sensor.BaseSensorEvent;


public interface EventService {

    void collectSensorEvent(BaseSensorEvent event);

    void collectHubEvent(BaseHubEvent event);
}