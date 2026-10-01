package com.chitra.urlshortener.repository;

import com.chitra.urlshortener.domain.IdempotencyRecord;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {
    Optional<IdempotencyRecord> findByUserIdAndKey(Long userId, String key);
    void deleteByCreatedAtBefore(Instant cutoff);
}