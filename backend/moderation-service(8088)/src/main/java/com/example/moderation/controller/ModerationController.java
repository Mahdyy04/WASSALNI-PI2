package com.example.moderation.controller;

import com.example.moderation.dto.ModerationRequest;
import com.example.moderation.service.ContentFilterService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    private final ContentFilterService filterService;

    public ModerationController(ContentFilterService filterService) {
        this.filterService = filterService;
    }

    @PostMapping("/check")
    public Mono<ResponseEntity<?>> check(@RequestBody ModerationRequest request) {

        String text = request.text();

        // Fast local check first
        if (filterService.containsLocalBadWords(text)) {
            return Mono.just(ResponseEntity.ok(Map.of("allowed", false, "reason", "Local profanity detected")));
        }

        // else call Gemini
        return filterService.checkWithGemini(text)
                .map(decision -> {
                    if (decision.allowed()) {
                        return ResponseEntity.ok(Map.of("allowed", true, "reason", decision.reason(), "modelRaw", decision.modelRaw()));
                    } else {
                        return ResponseEntity.ok(Map.of("allowed", false, "reason", decision.reason(), "modelRaw", decision.modelRaw()));
                    }
                });
    }
}