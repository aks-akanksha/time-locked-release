package com.example.timelock.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Component
public class ReleaseActionClient {
    private static final Logger log = LoggerFactory.getLogger(ReleaseActionClient.class);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);
    
    private final WebClient http;
    private final String webhookUrl;

    public ReleaseActionClient(@Value("${app.release.webhook-url:}") String webhookUrl) {
        this.http = WebClient.builder()
            .build();
        this.webhookUrl = webhookUrl;
    }

    public void trigger(Long releaseId, String title, String payloadJson) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.debug("Webhook URL not configured, skipping webhook trigger for release {}", releaseId);
            return;
        }
        
        Map<String, Object> payload = Map.of(
            "releaseId", releaseId,
            "title", title != null ? title : "",
            "payload", payloadJson != null ? payloadJson : ""
        );
        
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRIES) {
            try {
                log.debug("Attempting webhook call for release {} (attempt {}/{})", 
                    releaseId, attempt + 1, MAX_RETRIES);
                
                http.post()
                   .uri(webhookUrl)
                   .contentType(MediaType.APPLICATION_JSON)
                   .bodyValue(payload)
                   .retrieve()
                   .toBodilessEntity()
                   .timeout(Duration.ofSeconds(10))
                   .block();
                
                log.info("Webhook triggered successfully for release {} on attempt {}", 
                    releaseId, attempt + 1);
                return; // Success
                
            } catch (WebClientResponseException e) {
                lastException = e;
                int statusCode = e.getStatusCode().value();
                // Don't retry on 4xx client errors (except 408, 429)
                if (statusCode >= 400 && statusCode < 500 && statusCode != 408 && statusCode != 429) {
                    log.warn("Webhook call failed with client error {} for release {}: {}", 
                        statusCode, releaseId, e.getMessage());
                    throw new RuntimeException("Webhook call failed: " + e.getMessage(), e);
                }
                log.warn("Webhook call failed for release {} (attempt {}): {}", 
                    releaseId, attempt + 1, e.getMessage());
            } catch (Exception e) {
                lastException = e;
                log.warn("Webhook call failed for release {} (attempt {}): {}", 
                    releaseId, attempt + 1, e.getMessage());
            }
            
            attempt++;
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY.toMillis() * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting to retry webhook for release {}", releaseId);
                    throw new RuntimeException("Webhook retry interrupted", ie);
                }
            }
        }
        
        // All retries failed
        log.error("Webhook call failed after {} attempts for release {}", MAX_RETRIES, releaseId, lastException);
        throw new RuntimeException("Webhook call failed after " + MAX_RETRIES + " attempts", lastException);
    }
}
