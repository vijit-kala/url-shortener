package com.vk.url_shortner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vk.url_shortner.model.UrlMapping;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class UrlCacheTest {

    @Test
    void putShouldStoreMappingAndMakeItRetrievable() {
        UrlCache cache = new UrlCache();
        UrlMapping mapping = mapping(1L, "abc123", "https://example.com");

        cache.put(mapping);

        Optional<UrlMapping> result = cache.get("abc123");
        assertTrue(result.isPresent());
        assertSame(mapping, result.get());
        assertTrue(cache.contains("abc123"));
        assertEquals(1, cache.size());
    }

    @Test
    void getShouldReturnEmptyWhenShortKeyIsMissing() {
        UrlCache cache = new UrlCache();

        assertTrue(cache.get("missing").isEmpty());
        assertFalse(cache.contains("missing"));
        assertEquals(0, cache.size());
    }

    @Test
    void removeShouldDeleteStoredMapping() {
        UrlCache cache = new UrlCache();
        UrlMapping mapping = mapping(1L, "remove-me", "https://example.com/remove");
        cache.put(mapping);

        cache.remove("remove-me");

        assertTrue(cache.get("remove-me").isEmpty());
        assertFalse(cache.contains("remove-me"));
        assertEquals(0, cache.size());
    }

    @Test
    void putShouldReplaceExistingMappingForSameShortKey() {
        UrlCache cache = new UrlCache();
        UrlMapping first = mapping(1L, "same-key", "https://example.com/first");
        UrlMapping replacement = mapping(2L, "same-key", "https://example.com/second");
        cache.put(first);

        cache.put(replacement);

        Optional<UrlMapping> result = cache.get("same-key");
        assertTrue(result.isPresent());
        assertSame(replacement, result.get());
        assertEquals(1, cache.size());
    }

    private static UrlMapping mapping(Long id, String shortKey, String originalUrl) {
        return new UrlMapping(id, originalUrl, shortKey, LocalDateTime.of(2026, 3, 13, 8, 0), 0);
    }
}
