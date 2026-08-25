package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

public interface HubEventHandler<T> {

    void handle(String hubId, T payload);

}