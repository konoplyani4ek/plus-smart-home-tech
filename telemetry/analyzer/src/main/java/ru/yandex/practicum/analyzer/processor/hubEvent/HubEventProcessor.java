package ru.yandex.practicum.analyzer.processor.hubEvent;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.kafka.KafkaProperties;
import ru.yandex.practicum.analyzer.processor.hubEvent.handler.HandlerRegistry;
import ru.yandex.practicum.analyzer.processor.hubEvent.handler.HubEventHandler;
import ru.yandex.practicum.analyzer.serialization.HubEventAvroDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;


import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Component
public class HubEventProcessor implements Runnable {
    private final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);
    private final KafkaProperties kafkaProperties;
    private final HandlerRegistry handlerRegistry;

    public HubEventProcessor(KafkaProperties kafkaProperties, HandlerRegistry handlerRegistry) {
        this.kafkaProperties = kafkaProperties;
        this.handlerRegistry = handlerRegistry;
    }

    @Override
    public void run() {
        List<String> consumerTopics = List.of(kafkaProperties.consumer().hubEventProcessor().topic());
        Duration pollTimeout = Duration.ofMillis(kafkaProperties.consumer().pollTimeout());

        while (true) {
            Thread shutdownHook = null;

            try (KafkaConsumer<Void, HubEventAvro> consumer = createConsumer()) {
                shutdownHook = new Thread(consumer::wakeup);
                Runtime.getRuntime().addShutdownHook(shutdownHook);

                consumer.subscribe(consumerTopics);

                while (true) {
                    ConsumerRecords<Void, HubEventAvro> records = consumer.poll(pollTimeout);

                    if (records.isEmpty()) {
                        continue;
                    }

                    for (ConsumerRecord<Void, HubEventAvro> record : records) {
                        HubEventAvro event = record.value();

                        SpecificRecordBase payload = (SpecificRecordBase) event.getPayload();
                        HubEventHandler handler = handlerRegistry.getHandler(payload.getClass());

                        if (handler == null) {
                            continue;
                        }

                        try {
                            handler.handle(event.getHubId(), payload);
                        } catch (DataAccessException dae) {
                            log.error("DataAccessException handler={}", handler.getClass(), dae);
                        }

                        log.debug("polled HubEventAvro value={}, payload type={}", event, event.getPayload().getClass().getName());
                    }

                    consumer.commitSync();
                }
            } catch (WakeupException ignore) {
                log.info("Завершение работы HubEventProcessor");
            } catch (Exception e) {
                log.error("Ошибка в цикле обработки данных HubEventProcessor", e);
                if (shutdownHook != null) {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                }
            }

            Long retryPeriodMs = kafkaProperties.retryPeriodMs();
            if (retryPeriodMs == null) {
                return;
            }
            if (kafkaProperties.retryPeriodMs() != 0) {
                try {
                    Thread.sleep(retryPeriodMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

    }

    private KafkaConsumer<Void, HubEventAvro> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaProperties.consumer().hubEventProcessor().clientId());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.consumer().hubEventProcessor().groupId());
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.server());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventAvroDeserializer.class);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Отключение автокомита
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // При вхождении в новую группу забирать с начала

        return new KafkaConsumer<>(properties);
    }


}