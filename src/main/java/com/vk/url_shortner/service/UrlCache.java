package com.vk.url_shortner.service;

import com.vk.url_shortner.model.UrlMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UrlCache {

    private final ConcurrentHashMap<String, UrlMapping> cache = new ConcurrentHashMap<>();

    public void put(UrlMapping mapping) {
        cache.put(mapping.getShortKey(), mapping);
    }

    public Optional<UrlMapping> get(String shortKey) {
        return Optional.ofNullable(cache.get(shortKey));
    }

    public boolean contains(String shortKey) {
        return cache.containsKey(shortKey);
    }

    public void remove(String shortKey) {
        cache.remove(shortKey);
    }

    public int size() {
        return cache.size();
    }
}
