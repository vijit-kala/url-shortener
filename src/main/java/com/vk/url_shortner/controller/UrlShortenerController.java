package com.vk.url_shortner.controller;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.service.RateLimitedService;
import com.vk.url_shortner.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@Tag(name = "URL Shortener", description = "Operations related to shortening and resolving URLs")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService service;

    @Autowired
    private RateLimitedService rateLimitedService;

    @Operation(summary = "Shorten a URL", description = "Takes a long URL and returns a shortened version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shortened URL generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input URL")
    })
    @PostMapping("/shorten")
    public ResponseEntity<String> shorten(@RequestBody String originalUrl) {
        UrlMapping mapping = service.shortenUrl(originalUrl);
        return ResponseEntity.ok("http://localhost:8080/" + mapping.getShortKey());
    }

    @Operation(summary = "Resolve a short URL", description = "Redirects to the original long URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "404", description = "Short key not found")
    })
    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey,
                                         HttpServletResponse response) throws IOException {
        String originalUrl = service.getOriginalUrl(shortKey);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, originalUrl)
                .build();
    }

    @Operation(
            summary = "Get original URL asynchronously",
            description = "Fetches the original long URL for a given short key using an async service. " +
                    "If the rate limit is exceeded, returns HTTP 429 (Too Many Requests)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Original URL returned successfully"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded, try again later"),
            @ApiResponse(responseCode = "404", description = "Short key not found")
    })
    @GetMapping("/async/{shortKey}")
    public ResponseEntity<String> getOriginalUrlAsync(@PathVariable String shortKey) {
        if(!rateLimitedService.tryAcquire()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded, try again later");
        }
        return ResponseEntity.ok(service.getOriginalUrlAsync(shortKey).join());
    }
}
