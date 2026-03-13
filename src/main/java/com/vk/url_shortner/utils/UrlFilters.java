package com.vk.url_shortner.utils;

import com.vk.url_shortner.model.UrlMapping;

import java.util.function.Predicate;

public class UrlFilters {

    public static Predicate<UrlMapping> hasClicksGreaterThan(int threshold) {
        return mapping -> mapping.getClickCount() > threshold;
    }

    public static Predicate<UrlMapping> belongsToDomain(String domain) {
        return mapping -> mapping.getOriginalUrl().contains(domain);
    }
}
