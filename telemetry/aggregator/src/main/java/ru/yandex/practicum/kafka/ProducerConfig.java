package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Configuration
public class ProducerConfig {

    @Bean
    public Producer<Void, SpecificRecordBase> kafkaProducer(@Value("${kafka.server}") String kafkaServer) {
        Properties config = new Properties();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);

        Producer<Void, SpecificRecordBase> producer = new KafkaProducer<>(config);

        return producer;
    }

}