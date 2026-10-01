package com.chitra.urlshortener.repository;

import com.chitra.urlshortener.domain.ClickEvent;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    boolean existsByEventId(String eventId);
    Page<ClickEvent> findAllByUrlIdOrderByClickedAtDesc(Long urlId, Pageable pageable);
    long countByUrlId(Long urlId);

    @Query("select function('date', e.clickedAt), count(e) from ClickEvent e where e.url.id = :urlId group by function('date', e.clickedAt) order by function('date', e.clickedAt)")
    List<Object[]> countByDay(@Param("urlId") Long urlId);

    @Query("select count(e) from ClickEvent e where e.url.id = :urlId and e.clickedAt >= :since")
    long countSince(@Param("urlId") Long urlId, @Param("since") Instant since);
}