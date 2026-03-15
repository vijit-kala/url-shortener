package com.vk.url_shortner.service;

import com.vk.url_shortner.exception.InvalidUrlException;
import com.vk.url_shortner.exception.UrlNotFoundException;
import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import com.vk.url_shortner.utils.UrlCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class UrlShortenerService {

    @Autowired
    private UrlMappingRepository repository;

    private final UrlCache urlCache = new UrlCache();
    private final UrlCodec<String> codec = new UrlCodec<>();

    public UrlMapping shortenUrl(String originalUrl) {

        log.info("Shortening URL: {}", originalUrl);

        if (originalUrl == null || originalUrl.isBlank()) {
            log.error("Invalid URL input: {}", originalUrl);
            throw new InvalidUrlException(originalUrl);
        }

        String shortKey = generateShortKey();
        // encoding before saving
        String encodedUrl = codec.encode(originalUrl);

        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(encodedUrl)
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
            return codec.decode(updated.getOriginalUrl());
        }

        log.debug("Cache miss, querying DB for shortKey: {}", shortKey);

        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new UrlNotFoundException(shortKey));

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
        return codec.decode(updatedMapping.getOriginalUrl());
    }

    public CompletableFuture<String> getOriginalUrlAsync(String shortKey) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<UrlMapping> cached = urlCache.get(shortKey);
            if (cached.isPresent()) {
                UrlMapping updated = cached.get().toBuilder()
                        .clickCount(cached.get().getClickCount() + 1)
                        .build();

                urlCache.put(updated);
                repository.save(updated);
                return codec.decode(updated.getOriginalUrl());
            }

            UrlMapping mapping = repository.findByShortKey(shortKey)
                    .orElseThrow(() -> new UrlNotFoundException(shortKey));

            UrlMapping updatedMapping = mapping.toBuilder()
                    .clickCount(mapping.getClickCount() + 1)
                    .build();

            repository.save(updatedMapping);
            urlCache.put(updatedMapping);
            return codec.decode(updatedMapping.getOriginalUrl());
        });
    }

    private String generateShortKey() {
        String key = RandomStringUtils.randomAlphanumeric(8);
        log.debug("Generated shortKey: {}", key);
        return key;
    }
}
