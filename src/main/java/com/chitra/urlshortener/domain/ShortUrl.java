package com.chitra.urlshortener.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "urls", uniqueConstraints = @UniqueConstraint(name = "uk_urls_short_code", columnNames = "short_code"))
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_urls_user"))
    private UserEntity owner;

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 64)
    private String shortCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UrlStatus status = UrlStatus.ACTIVE;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected ShortUrl() { }

    public ShortUrl(UserEntity owner, String originalUrl, String shortCode, Instant expiresAt) {
        this.owner = owner;
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
    }

    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public void assignShortCode(String shortCode) { this.shortCode = shortCode; }
    public void disable() { this.status = UrlStatus.DISABLED; }
    public Long getId() { return id; }
    public UserEntity getOwner() { return owner; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public UrlStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}