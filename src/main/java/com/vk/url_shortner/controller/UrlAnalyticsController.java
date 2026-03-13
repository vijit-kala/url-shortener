package com.vk.url_shortner.controller;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.service.UrlAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class UrlAnalyticsController {

    @Autowired
    private UrlAnalyticsService service;

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalClicks() {
        return ResponseEntity.ok(service.getTotalClicks());
    }

    @GetMapping("/top")
    public ResponseEntity<List<UrlMapping>> getTopUrls(@RequestParam int limit) {
        return ResponseEntity.ok(service.getTopUrls(limit));
    }

    @GetMapping("/top/{minClicks}")
    public ResponseEntity<List<UrlMapping>> getPopularUrls(@PathVariable int minClicks) {
        return ResponseEntity.ok(service.getPopularUrls(minClicks));
    }

    @GetMapping("/domain")
    public ResponseEntity<Map<String, Long>> getClicksByDomain() {
        return ResponseEntity.ok(service.getClicksByDomain());
    }

    @GetMapping("/domain/{specDomain}")
    public ResponseEntity<List<UrlMapping>> getUrlsByDomain(@PathVariable String specDomain) {
        return ResponseEntity.ok(service.getUrlsByDomain(specDomain));
    }

}
