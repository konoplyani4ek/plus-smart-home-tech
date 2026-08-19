package ru.yandex.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.model.hub.*;


import java.util.ArrayList;
import java.util.List;

public class AvroHubEventMapper {
    private AvroHubEventMapper() {

    }

    public static HubEventAvro hubEventToAvro(BaseHubEvent hubEvent) {
        if (hubEvent == null) {
            throw new IllegalArgumentException("Hub event can't be null");
        }

        HubEventAvro hubEventAvro = new HubEventAvro();
        hubEventAvro.setHubId(hubEvent.getHubId());
        hubEventAvro.setTimestamp(hubEvent.getTimestamp());

        SpecificRecordBase payload;
        if (hubEvent.getClass() == DeviceAddedEvent.class) {
            payload = toDeviceAddedEventAvro((DeviceAddedEvent) hubEvent);
        } else if (hubEvent.getClass() == DeviceRemovedEvent.class) {
            payload = toDeviceRemovedEventAvro((DeviceRemovedEvent) hubEvent);
        } else if (hubEvent.getClass() == ScenarioAddedEvent.class) {
            payload = toScenarioAddedEventAvro((ScenarioAddedEvent) hubEvent);
        } else if (hubEvent.getClass() == ScenarioRemovedEvent.class) {
            payload = toScenarioRemovedEventAvro((ScenarioRemovedEvent) hubEvent);
        } else {
            throw new IllegalArgumentException(String.format("Unknown hub event type: %s", hubEvent.getClass()));
        }

        hubEventAvro.setPayload(payload);

        return hubEventAvro;
    }

    private static DeviceAddedEventAvro toDeviceAddedEventAvro(DeviceAddedEvent obj) {
        DeviceAddedEventAvro avroObj = new DeviceAddedEventAvro();

        avroObj.setId(obj.getId());
        avroObj.setType(toDeviceTypeAvro(obj.getDeviceType()));

        return avroObj;
    }

    private static DeviceRemovedEventAvro toDeviceRemovedEventAvro(DeviceRemovedEvent obj) {
        DeviceRemovedEventAvro avroObj = new DeviceRemovedEventAvro();

        avroObj.setId(obj.getId());

        return avroObj;
    }

    private static ScenarioAddedEventAvro toScenarioAddedEventAvro(ScenarioAddedEvent obj) {
        ScenarioAddedEventAvro avroObj = new ScenarioAddedEventAvro();

        avroObj.setName(obj.getName());

        List<DeviceActionAvro> actions = new ArrayList<>();
        for (DeviceAction action : obj.getActions()) {
            actions.add(toDeviceActionAvro(action));
        }
        avroObj.setActions(actions);

        List<ScenarioConditionAvro> conditions = new ArrayList<>();
        for (ScenarioCondition condition : obj.getConditions()) {
            conditions.add(toScenarioConditionAvro(condition));
        }
        avroObj.setConditions(conditions);

        return avroObj;
    }

    private static ScenarioRemovedEventAvro toScenarioRemovedEventAvro(ScenarioRemovedEvent obj) {
        ScenarioRemovedEventAvro avroObj = new ScenarioRemovedEventAvro();

        avroObj.setName(obj.getName());

        return avroObj;
    }

    private static DeviceActionAvro toDeviceActionAvro(DeviceAction obj) {
        DeviceActionAvro avroObj = new DeviceActionAvro();

        avroObj.setType(toActionTypeAvro(obj.getType()));
        avroObj.setValue(obj.getValue());
        avroObj.setSensorId(obj.getSensorId());

        return avroObj;
    }

    private static DeviceTypeAvro toDeviceTypeAvro(DeviceType deviceType) {
        if (deviceType == null) {
            return null;
        }

        return DeviceTypeAvro.valueOf(deviceType.name());
    }

    private static ActionTypeAvro toActionTypeAvro(ActionType obj) {
        if (obj == null) {
            return null;
        }
        return ActionTypeAvro.valueOf(obj.name());
    }

    private static ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition obj) {
        ScenarioConditionAvro avroObj = new ScenarioConditionAvro();

        if (obj.getValue() != null) {
            avroObj.setValue(obj.getValue());
        } else if (obj.getBoolValue() != null) {
            avroObj.setValue(obj.getBoolValue());
        } else {
            avroObj.setValue(null);
        }

        avroObj.setType(toConditionTypeAvro(obj.getType()));
        avroObj.setOperation(toConditionOperationAvro(obj.getOperation()));
        avroObj.setSensorId(obj.getSensorId());

        return avroObj;
    }

    private static ConditionTypeAvro toConditionTypeAvro(ConditionType obj) {
        if (obj == null) {
            return null;
        }
        return ConditionTypeAvro.valueOf(obj.name());
    }

    private static ConditionOperationAvro toConditionOperationAvro(ConditionOperation obj) {
        if (obj == null) {
            return null;
        }
        return ConditionOperationAvro.valueOf(obj.name());
    }

}