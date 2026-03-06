package com.vk.url_shortner.controller;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService service;

    @PostMapping("/shorten")
    public ResponseEntity<String> shorten(@RequestBody String originalUrl) {
        UrlMapping mapping = service.shortenUrl(originalUrl);
        return ResponseEntity.ok("http://localhost:8080/" + mapping.getShortKey());
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey,
                                         HttpServletResponse response) throws IOException {
        String originalUrl = service.getOriginalUrl(shortKey);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }
}
