package com.chitra.urlshortener.analytics;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.chitra.urlshortener.domain.ShortUrl;

@Service
public class UrlClickAnalyticsProducer {
    private final KafkaTemplate<String, ClickAnalyticsEvent> kafkaTemplate;
    private final String topic;

    public UrlClickAnalyticsProducer(
            KafkaTemplate<String, ClickAnalyticsEvent> kafkaTemplate,
            @Value("${app.kafka.topic:url-click-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishClick(ShortUrl shortUrl, String ipAddress, String userAgent, String referrer) {
        if (shortUrl == null || shortUrl.getShortCode() == null) {
            return;
        }

        ClickAnalyticsEvent event = new ClickAnalyticsEvent(
                UUID.randomUUID().toString(),
                shortUrl.getShortCode(),
                shortUrl.getOriginalUrl(),
                shortUrl.getOwner() != null ? shortUrl.getOwner().getId() : null,
                shortUrl.getOwner() != null ? shortUrl.getOwner().getEmail() : null,
                ipAddress,
                userAgent,
                referrer,
                Instant.now());

        kafkaTemplate.send(topic, event.eventId(), event);
    }
}
