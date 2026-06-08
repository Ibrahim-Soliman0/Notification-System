package org.registrationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
@Slf4j
@RequiredArgsConstructor
public class LogsController {

    private final RestTemplate restTemplate;

    @Value("${audit.service.url}")
    private String auditServiceUrl;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getLogs() {
        try {
            String url = auditServiceUrl + "/api/audit/logs";
            
            List<Map<String, Object>> logs = restTemplate.getForObject(
                    url,
                    List.class
            );
            
            log.info("Fetched {} logs from audit service", logs != null ? logs.size() : 0);
            
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error fetching logs from audit service", e);
            return ResponseEntity.ok(List.of());
        }
    }
}

