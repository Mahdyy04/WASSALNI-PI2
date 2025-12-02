package com.example.moderation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

// Imports Jackson pour le parsing JSON robuste
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ContentFilterService {

    private final WebClient webClient;
    private final String geminiUrl;
    private final String apiKey;
    private final Set<String> badWords = new HashSet<>();
    private final ObjectMapper objectMapper; // Champ pour Jackson

    public ContentFilterService(@Value("${moderation.gemini.url}") String geminiUrl,
                                @Value("${google.api.key}") String apiKey,
                                WebClient.Builder webClientBuilder,
                                ObjectMapper objectMapper) { // Injection d'ObjectMapper
        this.geminiUrl = geminiUrl;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        // CORRECTION : Ne pas utiliser baseUrl ici.
        this.webClient = webClientBuilder.build(); 
    }

    @PostConstruct
    public void loadBadWords() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("bad-words.txt");
        if (is == null) return;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            r.lines().map(String::trim).filter(s -> !s.isEmpty()).forEach(w -> badWords.add(w.toLowerCase()));
        } catch (Exception e) {
            // log
        }
    }

    // Local quick check
    public boolean containsLocalBadWords(String text) {
        if (text == null) return false;
        String n = text.toLowerCase();
        return badWords.stream().anyMatch(n::contains);
    }

    public Mono<ModerationDecision> checkWithGemini(String text) {
        String prompt = """
        Please classify the following text for vulgar / abusive content.
        Answer as JSON only, starting with the bracket: { "label": "SAFE" | "UNSAFE", "reason": "short text why" }.
        Text: "%s"
        """.formatted(text.replace("\n", " "));

        // Structure du Body JSON pour Gemini
        var body = Map.of(
            "contents", List.of(
                Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", prompt))
                )
            ),
            "generationConfig", Map.of(
                "temperature", 0.0
            )
        );
        
        // Construction de l'URL complète avec la clé API
        String fullGeminiUrl = this.geminiUrl + "?key=" + this.apiKey;

        return webClient.post()
                .uri(fullGeminiUrl) 
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                         response -> response.bodyToMono(String.class)
                            .flatMap(bodyString -> Mono.error(new RuntimeException("API Gemini Error: " + bodyString))))
                
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(6))
                .map(rawText -> {
                    // Utilisation du parsing Jackson pour extraire le texte en toute sécurité
                    String modelOutputJson = extractTextFromGeminiResponse(rawText);
                    
                    boolean allowed = true; // Initialiser à TRUE par défaut
                    String reason = "No issues detected by model";
        
                    try {
                        // Analyser le JSON que le modèle a généré (qui est modelOutputJson)
                        JsonNode modelDecision = objectMapper.readTree(modelOutputJson);
                        String label = modelDecision.path("label").asText("");
                        
                        // CORRECTION CRITIQUE DE LA LOGIQUE: Déterminer 'allowed' basé sur le label
                        if ("UNSAFE".equalsIgnoreCase(label)) {
                            allowed = false; // Le contenu est dangereux
                            reason = modelDecision.path("reason").asText("Detected by model");
                        } else if ("SAFE".equalsIgnoreCase(label)) {
                            allowed = true; // Le contenu est sûr
                            reason = modelDecision.path("reason").asText("No issues detected by model");
                        } else {
                             // Cas inattendu
                             reason = "Unexpected model label: " + label;
                        }

                    } catch (Exception e) {
                        // En cas d'erreur de parsing du JSON de sortie du modèle
                        reason = "Error parsing model output: " + e.getMessage();
                    }
                    
                    return new ModerationDecision(allowed, reason, rawText);
                })
                .onErrorResume(ex -> Mono.just(new ModerationDecision(true, "model_error_fallback_allowed: " + ex.getMessage(), ex.getMessage())));
    }

    /**
     * Helper pour extraire le texte généré par le modèle de la réponse JSON.
     * Cette version nettoie les blocs de code Markdown (```json...```)
     */
    private String extractTextFromGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            
            if (!textNode.isMissingNode() && textNode.isTextual()) {
                String rawText = textNode.asText();
                
                // CORRECTION: Nettoyer la chaîne pour supprimer les blocs de code Markdown
                if (rawText.startsWith("```json")) {
                    rawText = rawText.replace("```json", "")
                                   .replace("```", "")
                                   .trim();
                }
                
                return rawText;
            }
            
            return rawResponse;
        } catch (Exception e) {
            return rawResponse;
        }
    }

    public record ModerationDecision(boolean allowed, String reason, String modelRaw) {}
}