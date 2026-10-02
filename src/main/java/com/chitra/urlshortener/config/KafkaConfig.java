package com.chitra.urlshortener.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.chitra.urlshortener.analytics.ClickAnalyticsEvent;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, ClickAnalyticsEvent> clickAnalyticsProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ClickAnalyticsEvent> clickAnalyticsKafkaTemplate(
            ProducerFactory<String, ClickAnalyticsEvent> clickAnalyticsProducerFactory) {
        return new KafkaTemplate<>(clickAnalyticsProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, ClickAnalyticsEvent> clickAnalyticsConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.chitra.urlshortener.analytics");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ClickAnalyticsEvent.class.getName());
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ClickAnalyticsEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClickAnalyticsEvent> clickAnalyticsKafkaListenerContainerFactory(
            ConsumerFactory<String, ClickAnalyticsEvent> clickAnalyticsConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ClickAnalyticsEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(clickAnalyticsConsumerFactory);
        return factory;
    }
}
