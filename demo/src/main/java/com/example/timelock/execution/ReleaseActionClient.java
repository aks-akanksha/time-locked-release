package com.example.timelock.execution;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class ReleaseActionClient {
    private final WebClient http;
    private final String webhookUrl;

    public ReleaseActionClient(@Value("${app.release.webhook-url:}") String webhookUrl) {
        this.http = WebClient.builder().build();
        this.webhookUrl = webhookUrl;
    }

    public void trigger(Long releaseId, String title, String payloadJson) {
        if (webhookUrl == null || webhookUrl.isBlank()) return; // no-op if not configured
        http.post()
           .uri(webhookUrl)
           .contentType(MediaType.APPLICATION_JSON)
           .bodyValue(Map.of(
               "releaseId", releaseId,
               "title", title,
               "payload", payloadJson
           ))
           .retrieve()
           .toBodilessEntity()
           .block(); // fine for demo
    }
}
