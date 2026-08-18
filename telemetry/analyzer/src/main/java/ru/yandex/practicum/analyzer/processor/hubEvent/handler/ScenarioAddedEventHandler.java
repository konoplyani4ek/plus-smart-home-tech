package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.*;
import ru.yandex.practicum.analyzer.repository.ActionRepository;
import ru.yandex.practicum.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ScenarioAddedEventHandler implements HubEventHandler<ScenarioAddedEventAvro> {
    private final Logger log = LoggerFactory.getLogger(ScenarioAddedEventHandler.class);
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;

    public ScenarioAddedEventHandler(ActionRepository actionRepository,
                                     ConditionRepository conditionRepository,
                                     ScenarioRepository scenarioRepository) {
        this.actionRepository = actionRepository;
        this.conditionRepository = conditionRepository;
        this.scenarioRepository = scenarioRepository;
    }

    @Transactional
    @Override
    public void handle(String hubId, ScenarioAddedEventAvro payload) {
        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, payload.getName());
        if (searchResult.isPresent()) {
            log.info("Scenario for hubId={} and name={} is already exists", hubId, payload.getName());
            return;
        }

        Scenario scenario = new Scenario();
        scenario.setName(payload.getName());
        scenario.setHubId(hubId);

        // Собираем действия и запоминаем, какому sensorId какое действие соответствует
        List<DeviceActionAvro> actionsAvro = payload.getActions();
        List<Action> actions = new ArrayList<>(actionsAvro.size());
        for (DeviceActionAvro a : actionsAvro) {
            Action action = new Action();
            action.setValue(a.getValue());
            action.setType(ActionType.valueOf(a.getType().name()));
            actions.add(action);
        }
        // Один запрос вместо N
        List<Action> savedActions = actionRepository.saveAll(actions);
        for (int i = 0; i < savedActions.size(); i++) {
            scenario.getActions().put(actionsAvro.get(i).getSensorId(), savedActions.get(i));
        }
        log.info("created {} actions", savedActions.size());

        // Аналогично для условий
        List<ScenarioConditionAvro> conditionsAvro = payload.getConditions();
        List<Condition> conditions = new ArrayList<>(conditionsAvro.size());
        for (ScenarioConditionAvro sc : conditionsAvro) {
            Condition condition = new Condition();
            condition.setOperation(ConditionOperation.valueOf(sc.getOperation().name()));
            condition.setType(ConditionType.valueOf(sc.getType().name()));

            Object value = sc.getValue();
            if (value == null) {
                // допустимо: union {null, int, boolean} может не содержать значения
            } else if (value.getClass() == Integer.class) {
                condition.setIntValue((Integer) value);
            } else if (value.getClass() == Boolean.class) {
                condition.setBoolValue((Boolean) value);
            } else {
                throw new IllegalArgumentException("Unknown value type : " + value.getClass());
            }

            conditions.add(condition);
        }
        // Один запрос вместо N
        List<Condition> savedConditions = conditionRepository.saveAll(conditions);
        for (int i = 0; i < savedConditions.size(); i++) {
            scenario.getConditions().put(conditionsAvro.get(i).getSensorId(), savedConditions.get(i));
        }
        log.info("created {} conditions", savedConditions.size());

        scenarioRepository.save(scenario);
        log.info("created scenario={}", scenario);
    }
}