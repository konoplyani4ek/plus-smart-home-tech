package ru.yandex.practicum.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.model.hub.BaseHubEvent;
import ru.yandex.practicum.model.sensor.BaseSensorEvent;
import ru.yandex.practicum.serializer.GeneralAvroSerializer;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    private final KafkaProducer<String, SpecificRecordBase> producer;

    public EventServiceImpl() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.put("key.serializer", StringSerializer.class.getName());
        properties.put("value.serializer", GeneralAvroSerializer.class.getName());
        this.producer = new KafkaProducer<>(properties);
    }

    @PostConstruct
    public void warmUp() {
        // Принудительно устанавливаем соединение с брокером и получаем метаданные
        // топиков сразу при старте приложения, чтобы первое реальное сообщение
        // не ждало установки соединения
        producer.partitionsFor(SENSORS_TOPIC);
        producer.partitionsFor(HUBS_TOPIC);
    }

    @Override
    public void collectSensorEvent(BaseSensorEvent event) {
        log.trace("Получено событие датчика: id={}, hubId={}, type={}",
                event.getId(), event.getHubId(), event.getType());

        SensorEventAvro avro = SensorEventMapper.mapToAvro(event);
        send(avro, event.getHubId(), event.getTimestamp().toEpochMilli(), SENSORS_TOPIC);
    }

    @Override
    public void collectHubEvent(BaseHubEvent event) {
        log.trace("Получено событие хаба: hubId={}, type={}",
                event.getHubId(), event.getType());

        HubEventAvro avro = HubEventMapper.mapToAvro(event);
        send(avro, event.getHubId(), event.getTimestamp().toEpochMilli(), HUBS_TOPIC);
    }

    /**
     * Отправляет Avro-событие в указанный топик Kafka.
     *
     * @param event          Avro-объект события (значение записи)
     * @param hubId          Идентификатор хаба — используется как ключ записи,
     *                       чтобы события одного хаба хранились в одной партиции
     *                       и читались в порядке возникновения.
     * @param eventTimestamp Метка времени, когда произошло само событие (не когда
     *                       оно долетело до брокера) — записи в партиции будут
     *                       упорядочены именно по этому времени.
     * @param topic          Имя топика, в который нужно отправить событие.
     */
    private void send(SpecificRecordBase event, String hubId, long eventTimestamp, String topic) {
        // Формируем запись для отправки в топик, при этом указываем ключ записи - это id хаба
        // это означает, что запись будет сохраняться в партицию в зависимости от id хаба, а это
        // в свою очередь означает, что записи относящиеся к одному хабу можно будет читать упорядоченно
        // т.к. кафка гарантирует очередность сообщений только в рамках партиции.
        // Также мы указываем таймстемп записи и используем для этого время возникновения события
        // это значит, что кафка будет упорядочивать записи по времени возникновения события, а не времени
        // когда брокер кафки получил сообщение
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(
                topic, // Имя топика куда будет осуществлена запись
                null, // Номер партиции (если null, то используется ключ для вычисления раздела)
                eventTimestamp, // Метка времени события
                hubId, // Метка времени события
                event // Значение события
        );

        String eventClass = event.getClass().getSimpleName();
        log.info("Отправляю событие {} (hubId={}) в топик {}", eventClass, hubId, topic);

        Future<RecordMetadata> futureResult = producer.send(record);
        producer.flush();
        try {
            RecordMetadata metadata = futureResult.get();
            log.info("Событие {} (hubId={}) сохранено в топик {}, партиция {}, смещение {}",
                    eventClass, hubId, metadata.topic(), metadata.partition(), metadata.offset());
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Не удалось записать событие {} (hubId={}) в топик {}", eventClass, hubId, topic, e);
        }
    }

    /**
     * Метод для закрытия ресурсов, связанных с обработчиком.
     * Завершает отправку сообщений в Kafka и закрывает продюсера.
     */
    @PreDestroy
    public void close() {
        log.info("Закрываю Kafka producer");
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}