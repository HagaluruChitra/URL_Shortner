package com.chitra.urlshortener.analytics;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.chitra.urlshortener.domain.ClickEvent;
import com.chitra.urlshortener.domain.ShortUrl;
import com.chitra.urlshortener.repository.ClickEventRepository;
import com.chitra.urlshortener.repository.ShortUrlRepository;

@Component
public class UrlClickAnalyticsConsumer {
    private static final Logger log = LoggerFactory.getLogger(UrlClickAnalyticsConsumer.class);

    private final ShortUrlRepository shortUrlRepository;
    private final ClickEventRepository clickEventRepository;

    public UrlClickAnalyticsConsumer(ShortUrlRepository shortUrlRepository, ClickEventRepository clickEventRepository) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.topic:url-click-events}", groupId = "${spring.kafka.consumer.group-id:url-shortener-analytics}")
    public void consume(ClickAnalyticsEvent event) {
        if (event == null || event.shortCode() == null) {
            return;
        }

        Optional<ShortUrl> shortUrlOptional = shortUrlRepository.findByShortCode(event.shortCode());
        if (shortUrlOptional.isEmpty()) {
            log.warn("Received click event for unknown short code: {}", event.shortCode());
            return;
        }

        ClickEvent persisted = new ClickEvent(
                event.eventId(),
                shortUrlOptional.get(),
                event.ipAddress(),
                event.userAgent(),
                event.referrer(),
                event.clickedAt());

        clickEventRepository.save(persisted);
    }
}
