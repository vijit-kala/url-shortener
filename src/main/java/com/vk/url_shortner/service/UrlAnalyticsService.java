package com.vk.url_shortner.service;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UrlAnalyticsService {

    private final UrlMappingRepository repository;

    public UrlAnalyticsService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    public int getTotalClicks() {
        return repository.findAll().stream()
                .mapToInt(UrlMapping::getClickCount)
                .sum();
    }

    public List<UrlMapping> getTopUrls(int limit) {
        return repository.findAll().stream()
                .sorted(Comparator.comparingInt(UrlMapping::getClickCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getClicksByDomain() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(
                        mapping -> extractDomain(mapping.getOriginalUrl()),
                        Collectors.summingLong(UrlMapping::getClickCount)
                ));
    }

    private String extractDomain(String url) {
        try {
            return new java.net.URL(url).getHost();
        } catch (Exception e) {
            return "invalid";
        }
    }


}
