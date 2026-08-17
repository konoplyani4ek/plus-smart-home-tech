package ru.yandex.practicum.analyzer.processor.hubEvent.handler;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;


import java.util.Optional;

@Component
public class ScenarioRemovedEventHandler implements HubEventHandler<ScenarioRemovedEventAvro> {
    private final Logger log = LoggerFactory.getLogger(ScenarioRemovedEventHandler.class);
    private final ScenarioRepository scenarioRepository;

    public ScenarioRemovedEventHandler(ScenarioRepository scenarioRepository) {
        this.scenarioRepository = scenarioRepository;
    }


    @Transactional
    @Override
    public void handle(String hubId, ScenarioRemovedEventAvro payload) {
        String name = payload.getName();
        Optional<Scenario> searchResult = scenarioRepository.findByHubIdAndName(hubId, name);

        if (searchResult.isEmpty()) {
            return;
        }
        // Все остальное удалится само т.к. CascadeType.REMOVE у Scenario.
        scenarioRepository.delete(searchResult.get());
        log.info("scenario removed name={}, hubId={}", name, hubId);
    }
}