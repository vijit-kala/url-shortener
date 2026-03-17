package com.vk.url_shortner.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vk.url_shortner.exception.InvalidUrlException;
import com.vk.url_shortner.exception.UrlNotFoundException;
import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.repository.UrlMappingRepository;
import com.vk.url_shortner.utils.UrlCodec;
import java.time.LocalDateTime;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class UrlShortenerServiceTest {

    private final UrlCodec<String> codec = new UrlCodec<>();

    @Test
    void shortenUrlShouldRejectNullInput() {
        UrlShortenerService service = serviceWithRepository();

        assertThrows(InvalidUrlException.class, () -> service.shortenUrl(null));
    }

    @Test
    void shortenUrlShouldRejectBlankInput() {
        UrlShortenerService service = serviceWithRepository();

        assertThrows(InvalidUrlException.class, () -> service.shortenUrl("   "));
    }

    @Test
    void shortenUrlShouldEncodeAndPersistMapping() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        ArgumentCaptor<UrlMapping> mappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping mapping = invocation.getArgument(0);
            return new UrlMapping(
                    99L,
                    mapping.getOriginalUrl(),
                    mapping.getShortKey(),
                    mapping.getCreatedAt(),
                    mapping.getClickCount()
            );
        });

        UrlMapping saved = service.shortenUrl("https://example.com/path");

        verify(repository).save(mappingCaptor.capture());
        UrlMapping persisted = mappingCaptor.getValue();
        assertEquals(codec.encode("https://example.com/path"), persisted.getOriginalUrl());
        assertNotNull(persisted.getCreatedAt());
        assertEquals(0, persisted.getClickCount());
        assertNotNull(persisted.getShortKey());
        assertEquals(8, persisted.getShortKey().length());
        assertTrue(((UrlCache) ReflectionTestUtils.getField(service, "urlCache")).contains(saved.getShortKey()));
        assertEquals(99L, saved.getId());
    }

    @Test
    void getOriginalUrlShouldUseCacheWhenAvailable() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        UrlMapping cached = mapping(1L, "abc12345", codec.encode("https://example.com/cached"), 2);
        UrlCache cache = (UrlCache) ReflectionTestUtils.getField(service, "urlCache");
        cache.put(cached);
        ArgumentCaptor<UrlMapping> mappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String originalUrl = service.getOriginalUrl("abc12345");

        assertEquals("https://example.com/cached", originalUrl);
        verify(repository, never()).findByShortKey("abc12345");
        verify(repository).save(mappingCaptor.capture());
        assertEquals(3, mappingCaptor.getValue().getClickCount());
        assertEquals(3, cache.get("abc12345").orElseThrow().getClickCount());
    }

    @Test
    void getOriginalUrlShouldLoadFromRepositoryOnCacheMiss() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        UrlMapping stored = mapping(7L, "miss1234", codec.encode("https://openai.com/page"), 5);
        ArgumentCaptor<UrlMapping> mappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repository.findByShortKey("miss1234")).thenReturn(Optional.of(stored));
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String originalUrl = service.getOriginalUrl("miss1234");

        assertEquals("https://openai.com/page", originalUrl);
        verify(repository).findByShortKey("miss1234");
        verify(repository).save(mappingCaptor.capture());
        assertEquals(6, mappingCaptor.getValue().getClickCount());
        UrlCache cache = (UrlCache) ReflectionTestUtils.getField(service, "urlCache");
        assertEquals(6, cache.get("miss1234").orElseThrow().getClickCount());
    }

    @Test
    void getOriginalUrlShouldThrowWhenShortKeyDoesNotExist() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        when(repository.findByShortKey("missing")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrl("missing"));
        verify(repository, never()).save(any(UrlMapping.class));
    }

    @Test
    void getOriginalUrlAsyncShouldUseCacheWhenAvailable() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        UrlMapping cached = mapping(11L, "async001", codec.encode("https://example.com/async-cache"), 4);
        UrlCache cache = (UrlCache) ReflectionTestUtils.getField(service, "urlCache");
        cache.put(cached);
        ArgumentCaptor<UrlMapping> mappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String originalUrl = service.getOriginalUrlAsync("async001").join();

        assertEquals("https://example.com/async-cache", originalUrl);
        verify(repository, never()).findByShortKey("async001");
        verify(repository).save(mappingCaptor.capture());
        assertEquals(5, mappingCaptor.getValue().getClickCount());
        assertEquals(5, cache.get("async001").orElseThrow().getClickCount());
    }

    @Test
    void getOriginalUrlAsyncShouldLoadFromRepositoryOnCacheMiss() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        UrlMapping stored = mapping(12L, "async002", codec.encode("https://example.com/async-db"), 1);
        ArgumentCaptor<UrlMapping> mappingCaptor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repository.findByShortKey("async002")).thenReturn(Optional.of(stored));
        when(repository.save(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String originalUrl = service.getOriginalUrlAsync("async002").join();

        assertEquals("https://example.com/async-db", originalUrl);
        verify(repository).findByShortKey("async002");
        verify(repository).save(mappingCaptor.capture());
        assertEquals(2, mappingCaptor.getValue().getClickCount());
        UrlCache cache = (UrlCache) ReflectionTestUtils.getField(service, "urlCache");
        assertEquals(2, cache.get("async002").orElseThrow().getClickCount());
    }

    @Test
    void getOriginalUrlAsyncShouldThrowWhenShortKeyDoesNotExist() {
        UrlMappingRepository repository = repositoryMock();
        UrlShortenerService service = serviceWithRepository(repository);
        when(repository.findByShortKey("async-missing")).thenReturn(Optional.empty());

        CompletionException exception = assertThrows(
                CompletionException.class,
                () -> service.getOriginalUrlAsync("async-missing").join()
        );

        assertTrue(exception.getCause() instanceof UrlNotFoundException);
        verify(repository, never()).save(any(UrlMapping.class));
    }

    private static UrlShortenerService serviceWithRepository() {
        return serviceWithRepository(repositoryMock());
    }

    private static UrlShortenerService serviceWithRepository(UrlMappingRepository repository) {
        Executor executor = Runnable::run;
        UrlShortenerService service = new UrlShortenerService(executor);
        ReflectionTestUtils.setField(service, "repository", repository);
        return service;
    }

    private static UrlMappingRepository repositoryMock() {
        return org.mockito.Mockito.mock(UrlMappingRepository.class);
    }

    private static UrlMapping mapping(Long id, String shortKey, String encodedUrl, int clickCount) {
        return new UrlMapping(id, encodedUrl, shortKey, LocalDateTime.of(2026, 3, 13, 8, 0), clickCount);
    }
}
