package com.chitra.urlshortener.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "click_events", uniqueConstraints = @UniqueConstraint(name = "uk_click_events_event_id", columnNames = "event_id"))
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_id", nullable = false, foreignKey = @ForeignKey(name = "fk_click_events_url"))
    private ShortUrl url;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(length = 2048)
    private String referrer;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    protected ClickEvent() { }

    public ClickEvent(String eventId, ShortUrl url, String ipAddress, String userAgent, String referrer, Instant clickedAt) {
        this.eventId = eventId;
        this.url = url;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.clickedAt = clickedAt;
    }

    public String getEventId() { return eventId; }
    public ShortUrl getUrl() { return url; }
    public Instant getClickedAt() { return clickedAt; }
    public String getUserAgent() { return userAgent; }
}