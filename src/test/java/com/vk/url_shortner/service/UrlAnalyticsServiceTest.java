package com.vk.url_shortner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UrlAnalyticsServiceTest {

    @Test
    void getTotalClicksShouldSumAllRepositoryClickCounts() {
        UrlAnalyticsService service = serviceWithMappings(
                mapping(1L, "alpha", "https://example.com/a", 4),
                mapping(2L, "beta", "https://example.com/b", 7),
                mapping(3L, "gamma", "https://openai.com/c", 2)
        );

        assertEquals(13, service.getTotalClicks());
    }

    @Test
    void getTopUrlsShouldReturnMappingsSortedByClickCountDescending() {
        UrlMapping low = mapping(1L, "low", "https://example.com/low", 1);
        UrlMapping high = mapping(2L, "high", "https://example.com/high", 10);
        UrlMapping mid = mapping(3L, "mid", "https://example.com/mid", 5);
        UrlAnalyticsService service = serviceWithMappings(low, high, mid);

        List<UrlMapping> topUrls = service.getTopUrls(2);

        assertIterableEquals(List.of(high, mid), topUrls);
    }

    @Test
    void getClicksByDomainShouldGroupClicksPerHostAndHandleInvalidUrls() {
        UrlAnalyticsService service = serviceWithMappings(
                mapping(1L, "a", "https://example.com/a", 3),
                mapping(2L, "b", "https://example.com/b", 4),
                mapping(3L, "c", "https://openai.com/c", 6),
                mapping(4L, "d", "not-a-valid-url", 2)
        );

        Map<String, Long> clicksByDomain = service.getClicksByDomain();

        assertEquals(7L, clicksByDomain.get("example.com"));
        assertEquals(6L, clicksByDomain.get("openai.com"));
        assertEquals(2L, clicksByDomain.get("invalid"));
    }

    @Test
    void filterUrlsShouldReturnOnlyMappingsThatMatchPredicate() {
        UrlMapping popular = mapping(1L, "popular", "https://example.com/popular", 9);
        UrlMapping quiet = mapping(2L, "quiet", "https://example.com/quiet", 1);
        UrlAnalyticsService service = serviceWithMappings(popular, quiet);

        List<UrlMapping> filtered = service.filterUrls(mapping -> mapping.getClickCount() >= 5);

        assertIterableEquals(List.of(popular), filtered);
    }

    @Test
    void getPopularUrlsShouldUseStrictlyGreaterThanMinimumClicks() {
        UrlMapping belowThreshold = mapping(1L, "below", "https://example.com/below", 4);
        UrlMapping equalThreshold = mapping(2L, "equal", "https://example.com/equal", 5);
        UrlMapping aboveThreshold = mapping(3L, "above", "https://example.com/above", 6);
        UrlAnalyticsService service = serviceWithMappings(belowThreshold, equalThreshold, aboveThreshold);

        List<UrlMapping> popular = service.getPopularUrls(5);

        assertIterableEquals(List.of(aboveThreshold), popular);
    }

    @Test
    void getUrlsByDomainShouldReturnMappingsContainingRequestedDomain() {
        UrlMapping example = mapping(1L, "example", "https://example.com/path", 1);
        UrlMapping openai = mapping(2L, "openai", "https://openai.com/path", 1);
        UrlAnalyticsService service = serviceWithMappings(example, openai);

        List<UrlMapping> urls = service.getUrlsByDomain("example.com");

        assertIterableEquals(List.of(example), urls);
    }

    @SafeVarargs
    private static UrlAnalyticsService serviceWithMappings(UrlMapping... mappings) {
        UrlMappingRepository repository = mock(UrlMappingRepository.class);
        when(repository.findAll()).thenReturn(List.of(mappings));
        return new UrlAnalyticsService(repository);
    }

    private static UrlMapping mapping(Long id, String shortKey, String originalUrl, int clickCount) {
        return new UrlMapping(id, originalUrl, shortKey, LocalDateTime.of(2026, 3, 13, 8, 0), clickCount);
    }
}
