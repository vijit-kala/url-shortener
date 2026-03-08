package com.vk.url_shortner.service;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlMappingRepository repository;

    public UrlMapping shortenUrl(String originalUrl) {
        String shortKey = generateShortKey();
        UrlMapping mapping = UrlMapping.builder()
                .originalUrl(originalUrl)
                .shortKey(shortKey)
                .createdAt(LocalDateTime.now())
                .clickCount(0)
                .build();
        return repository.save(mapping);
    }

    public String getOriginalUrl(String shortKey) {
        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("Not found"));

        UrlMapping updatedMapping = UrlMapping.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortKey(mapping.getShortKey())
                .createdAt(mapping.getCreatedAt())
                .clickCount(mapping.getClickCount() + 1)
                .build();

        repository.save(updatedMapping);
        return updatedMapping.getOriginalUrl();
    }

    private String generateShortKey() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
}
