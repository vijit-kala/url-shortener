package com.vk.url_shortner.service;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UrlShortenerService {

    @Autowired
    private UrlMappingRepository repository;

    private final UrlCache urlCache = new UrlCache();

    public UrlMapping shortenUrl(String originalUrl) {

        log.info("Shortening URL: {}", originalUrl);

        String shortKey = generateShortKey();
        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortKey(shortKey)
                .createdAt(LocalDateTime.now())
                .clickCount(0)
                .build();
        UrlMapping saved = repository.save(mapping);
        urlCache.put(saved);

        log.debug("Saved UrlMapping: {}", saved);
        return saved;
    }

    public String getOriginalUrl(String shortKey) {

        log.info("Fetching original URL for shortkey: {}", shortKey);

        // Checking cache first
        Optional<UrlMapping> cached = urlCache.get(shortKey);
        if (cached.isPresent()) {
            log.debug("Cache hit for shortKey: {}", shortKey);
            UrlMapping updated = cached.get().toBuilder()
                    .clickCount(cached.get().getClickCount() + 1)
                    .build();
            urlCache.put(updated);
            repository.save(updated);
            return updated.getOriginalUrl();
        }

        log.debug("Cache miss, querying DB for shortKey: {}", shortKey);

        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> {
                    log.error("ShortKey {} not found in DB", shortKey);
                    return new RuntimeException("Not Found");
                });

        UrlMapping updatedMapping = UrlMapping.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortKey(mapping.getShortKey())
                .createdAt(mapping.getCreatedAt())
                .clickCount(mapping.getClickCount() + 1)
                .build();

        repository.save(updatedMapping);
        urlCache.put(updatedMapping);

        log.info("Returning original URL for shortKey {}: {}", shortKey, updatedMapping.getOriginalUrl());
        return updatedMapping.getOriginalUrl();
    }

    private String generateShortKey() {
        String key = RandomStringUtils.randomAlphanumeric(8);
        log.debug("Generated shortKey: {}", key);
        return key;
    }
}
