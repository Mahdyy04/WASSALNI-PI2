package com.carpooling.app.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local content moderation service that checks for inappropriate content
 * without requiring external API calls
 */
class ContentModerationService(context: Context) {

    companion object {
        private const val TAG = "ContentModerationService"

        // List of inappropriate words/patterns to block
        private val INAPPROPRIATE_WORDS = setOf(
            // Profanity (common variations)
            "damn", "hell", "crap", "ass", "bitch", "bastard", "piss",
            "dick", "shit", "fuck", "cunt", "whore", "slut", "retard",
            "stupid", "dumb", "idiot", "moron", "jackass",

            // Offensive language
            "hate", "despise", "kill", "murder", "die", "dying",
            "attack", "threat", "threaten", "harass", "bully",

            // Discriminatory terms (common examples)
            "nigger", "faggot", "tranny", "rape", "molest",

            // Explicit content indicators
            "sex", "porn", "xxx", "naked", "nude", "horny",

            // Violence
            "violence", "violent", "fight", "punch", "hit", "kick"
        )
    }

    /**
     * Check if content contains inappropriate words
     * Returns true if inappropriate content is found
     */
    suspend fun isContentInappropriate(text: String): Boolean = withContext(Dispatchers.Default) {
        return@withContext try {
            val lowerText = text.lowercase()

            // Check for inappropriate words
            for (word in INAPPROPRIATE_WORDS) {
                if (lowerText.contains(word)) {
                    Log.d(TAG, "Inappropriate content detected: $word")
                    return@withContext true
                }
            }

            // Check for excessive exclamation marks or caps (potential harassment)
            val exclamationRatio = text.count { it == '!' }.toFloat() / text.length
            if (exclamationRatio > 0.1f) {
                Log.d(TAG, "Excessive exclamation marks detected")
                return@withContext true
            }

            val capsRatio = text.count { it.isUpperCase() }.toFloat() / text.length
            if (capsRatio > 0.5f && text.length > 10) {
                Log.d(TAG, "Excessive caps detected")
                return@withContext true
            }

            Log.d(TAG, "Content is appropriate")
            false

        } catch (e: Exception) {
            Log.e(TAG, "Error during content moderation: ${e.message}", e)
            // Safe fallback: allow content if check fails
            false
        }
    }

    /**
     * Get a moderation report
     */
    suspend fun getModerationReport(text: String): String = withContext(Dispatchers.Default) {
        return@withContext try {
            val lowerText = text.lowercase()
            val issues = mutableListOf<String>()

            for (word in INAPPROPRIATE_WORDS) {
                if (lowerText.contains(word)) {
                    issues.add("Contains inappropriate language: '$word'")
                }
            }

            if (issues.isEmpty()) {
                "Content is appropriate"
            } else {
                issues.joinToString("; ")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during moderation analysis: ${e.message}", e)
            "Unable to analyze content"
        }
    }
}

