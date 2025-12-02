package com.example.moderation.dto;

public record ModerationResult(boolean allowed, String reason, String modelRaw) {}