package com.vk.url_shortner.exception;

public class UrlNotFoundException extends RuntimeException {
    public UrlNotFoundException(String shortKey) {
        super("URL not found for shortKey: " + shortKey);
    }
}
