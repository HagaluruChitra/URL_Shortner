package com.chitra.urlshortener.analytics;

import java.time.Instant;

public record ClickAnalyticsEvent(
        String eventId,
        String shortCode,
        String originalUrl,
        Long userId,
        String userEmail,
        String ipAddress,
        String userAgent,
        String referrer,
        Instant clickedAt
) {
}
