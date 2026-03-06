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
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortKey(shortKey);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setClickCount(0);
        return repository.save(mapping);
    }

    public String getOriginalUrl(String shortKey) {
        UrlMapping mapping = repository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("Not found"));
        mapping.setClickCount(mapping.getClickCount() + 1);
        repository.save(mapping);
        return mapping.getOriginalUrl();
    }

    private String generateShortKey() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
}
