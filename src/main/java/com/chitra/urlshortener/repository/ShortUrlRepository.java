package com.chitra.urlshortener.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chitra.urlshortener.domain.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    Optional<ShortUrl> findByIdAndOwnerId(Long id, Long ownerId);
    Page<ShortUrl> findAllByOwnerId(Long ownerId, Pageable pageable);
    boolean existsByShortCode(String shortCode);
    long countByIdAndOwnerId(Long id, Long ownerId);
    long countByOwnerId(Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerIdAndExpiresAtBefore(Long id, Long ownerId, Instant instant);
}