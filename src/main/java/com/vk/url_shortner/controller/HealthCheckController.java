package com.vk.url_shortner.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Health check endpoint to verify service status")
public class HealthCheckController {

    @Operation(
            summary = "Health check",
            description = "Returns a simple message indicating that the service is running correctly."
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> getHealthCheck() {
        return ResponseEntity.ok("Working fine!");
    }
}
