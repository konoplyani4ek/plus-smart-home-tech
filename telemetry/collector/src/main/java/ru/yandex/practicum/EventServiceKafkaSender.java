package ru.yandex.practicum;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.AvroHubEventMapper;
import ru.yandex.practicum.mapper.AvroSensorEventMapper;
import ru.yandex.practicum.model.hub.BaseHubEvent;
import ru.yandex.practicum.model.sensor.BaseSensorEvent;
import ru.yandex.practicum.service.EventService;


@Service
public class EventServiceKafkaSender implements EventService {
    private final Producer<Void, SpecificRecordBase> kafkaProducer;
    private final String sensorEventsTopic;
    private final String hubEventsTopic;

    public EventServiceKafkaSender(Producer<Void, SpecificRecordBase> producer,
                                   @Value("${kafka.topic.sensor-events}") String sensorEventsTopic,
                                   @Value("${kafka.topic.hub-events}") String hubEventsTopic) {
        this.kafkaProducer = producer;
        this.sensorEventsTopic = sensorEventsTopic;
        this.hubEventsTopic = hubEventsTopic;
    }

    @Override
    public void sendSensorEvent(BaseSensorEvent sensorEvent) {
        SensorEventAvro sensorEventAvro = AvroSensorEventMapper.sensorEventToAvro(sensorEvent);

        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(sensorEventsTopic, sensorEventAvro);

        kafkaProducer.send(record);
    }

    @Override
    public void sendHubEvent(BaseHubEvent hubEvent) {
        HubEventAvro hubEventAvro = AvroHubEventMapper.hubEventToAvro(hubEvent);

        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(hubEventsTopic, hubEventAvro);

        kafkaProducer.send(record);
    }
}