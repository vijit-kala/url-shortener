package com.vk.url_shortner.controller;

import com.vk.url_shortner.model.UrlMapping;
import com.vk.url_shortner.service.UrlAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for URL analytics and reporting")
public class UrlAnalyticsController {

    @Autowired
    private UrlAnalyticsService service;

    @Operation(summary = "Get total clicks", description = "Returns the total number of clicks across all shortened URLs")
    @ApiResponse(responseCode = "200", description = "Total clicks returned successfully")
    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalClicks() {
        return ResponseEntity.ok(service.getTotalClicks());
    }

    @Operation(summary = "Get top URLs", description = "Returns the top URLs ranked by click count, limited by the given parameter")
    @ApiResponse(responseCode = "200", description = "Top URLs returned successfully")
    @GetMapping("/top")
    public ResponseEntity<List<UrlMapping>> getTopUrls(@RequestParam int limit) {
        return ResponseEntity.ok(service.getTopUrls(limit));
    }

    @Operation(summary = "Get popular URLs", description = "Returns URLs that have at least the specified minimum number of clicks")
    @ApiResponse(responseCode = "200", description = "Popular URLs returned successfully")
    @GetMapping("/top/{minClicks}")
    public ResponseEntity<List<UrlMapping>> getPopularUrls(@PathVariable int minClicks) {
        return ResponseEntity.ok(service.getPopularUrls(minClicks));
    }

    @Operation(summary = "Get clicks by domain", description = "Returns a map of domains and their total click counts")
    @ApiResponse(responseCode = "200", description = "Domain click counts returned successfully")
    @GetMapping("/domain")
    public ResponseEntity<Map<String, Long>> getClicksByDomain() {
        return ResponseEntity.ok(service.getClicksByDomain());
    }

    @Operation(summary = "Get URLs by domain", description = "Returns all URLs associated with the specified domain")
    @ApiResponse(responseCode = "200", description = "URLs for domain returned successfully")
    @GetMapping("/domain/{specDomain}")
    public ResponseEntity<List<UrlMapping>> getUrlsByDomain(@PathVariable String specDomain) {
        return ResponseEntity.ok(service.getUrlsByDomain(specDomain));
    }

    @Operation(summary = "Group by domain", description = "Groups URLs by their domain and returns click counts")
    @ApiResponse(responseCode = "200", description = "Grouped domain data returned successfully")
    @GetMapping("/group/domain")
    public ResponseEntity<Map<String, Long>> groupByDomain() {
        return ResponseEntity.ok(service.groupBy(m -> {
            try {
                return new java.net.URL(m.getOriginalUrl()).getHost();
            } catch (Exception e) {
                return "invalid";
            }
        }));
    }

    @Operation(summary = "Group by date", description = "Groups URLs by their creation date and returns counts")
    @ApiResponse(responseCode = "200", description = "Grouped date data returned successfully")
    @GetMapping("/group/date")
    public ResponseEntity<Map<LocalDateTime, Long>> groupByDate() {
        return ResponseEntity.ok(service.groupBy(UrlMapping::getCreatedAt));
    }

    @Operation(summary = "Aggregate clicks by domain", description = "Aggregates click counts grouped by domain")
    @ApiResponse(responseCode = "200", description = "Aggregated domain click counts returned successfully")
    @GetMapping("/aggregate/domain")
    public ResponseEntity<Map<String, Integer>> aggregateClicksByDomain() {
        return ResponseEntity.ok(service.aggregateBy(m -> {
            try {
                return new java.net.URL(m.getOriginalUrl()).getHost();
            } catch (Exception e) {
                return "invalid";
            }
        }));
    }

    @Operation(summary = "Get top URLs by clicks", description = "Returns the top URLs ranked by click count")
    @ApiResponse(responseCode = "200", description = "Top URLs by clicks returned successfully")
    @GetMapping("/top/clicks")
    public ResponseEntity<List<UrlMapping>> getTopByClicks(@RequestParam int limit) {
        return ResponseEntity.ok(service.getTop(limit,
                (a, b) -> Integer.compare(b.getClickCount(), a.getClickCount())));
    }

    @Operation(summary = "Get newest URLs", description = "Returns the most recently created URLs")
    @ApiResponse(responseCode = "200", description = "Newest URLs returned successfully")
    @GetMapping("/top/newest")
    public ResponseEntity<List<UrlMapping>> getNewest(@RequestParam int limit) {
        return ResponseEntity.ok(service.getTop(limit,
                (a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())));
    }
}
