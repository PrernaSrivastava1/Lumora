package com.lumora.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health Check Controller.
 */
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public Map<String, String> getHealth() {
        return Map.of(
                "status", "UP",
                "project", "Lumora AI"
        );
    }
}
