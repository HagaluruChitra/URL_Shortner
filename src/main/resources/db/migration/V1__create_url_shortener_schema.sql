CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS urls (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_url TEXT NOT NULL,
    short_code VARCHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_urls_short_code UNIQUE (short_code),
    CONSTRAINT fk_urls_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_urls_owner_created (user_id, created_at),
    INDEX idx_urls_expiration (status, expires_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS click_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id CHAR(36) NOT NULL,
    url_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(512) NULL,
    referrer VARCHAR(2048) NULL,
    clicked_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_click_events_event_id UNIQUE (event_id),
    CONSTRAINT fk_click_events_url FOREIGN KEY (url_id) REFERENCES urls (id) ON DELETE CASCADE,
    INDEX idx_click_events_url_time (url_id, clicked_at),
    INDEX idx_click_events_time (clicked_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash CHAR(64) NOT NULL,
    response_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_idempotency_user_key UNIQUE (user_id, idempotency_key),
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_idempotency_created (created_at)
) ENGINE=InnoDB;