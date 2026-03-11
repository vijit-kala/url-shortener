package com.vk.url_shortner.exception;

public class InvalidUrlException extends RuntimeException {
    public InvalidUrlException(String url) {
        super("Invalid URL provided: " + url);
    }
}
