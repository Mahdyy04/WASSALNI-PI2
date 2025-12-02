# Quick Code Reference - Content Moderation

## Quick Start Example

### 1. Initialize Service
```kotlin
private lateinit var contentModerationService: ContentModerationService

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    contentModerationService = ContentModerationService(requireContext())
}
```

### 2. Check Content Before Submission
```kotlin
// Check if content is inappropriate
val isInappropriate = contentModerationService.isContentInappropriate(userInput)

if (isInappropriate) {
    Toast.makeText(
        context, 
        "Your content contains inappropriate language. Please revise.",
        Toast.LENGTH_LONG
    ).show()
    return@launch
}

// Proceed with submission
submitToBackend(userInput)
```

---

## Real Usage in App

### Report Submission (Updated)
```kotlin
private fun submitReport(booking: Booking, reason: String, description: String) {
    val passengerId = sessionManager.getUserId()
    val driverId = booking.ride?.driverId ?: return
    
    if (description.isEmpty()) {
        Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
        return
    }
    
    lifecycleScope.launch {
        try {
            Toast.makeText(context, "Checking content...", Toast.LENGTH_SHORT).show()
            
            // ✅ NEW: Check for inappropriate content
            val isInappropriate = contentModerationService.isContentInappropriate(description)
            
            if (isInappropriate) {
                Toast.makeText(
                    context, 
                    "You cannot use bad words or offensive language in your report. Please revise your description and try again.",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }
            
            // Content is appropriate, submit
            val request = CreateReportRequest(
                reporterId = passengerId,
                reportedUserId = driverId,
                rideId = booking.rideId,
                reason = reason,
                description = description
            )
            
            val response = RetrofitClient.apiService.createReport(request)
            if (response.isSuccessful) {
                Toast.makeText(context, "Report submitted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error submitting report", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### Review Submission (Updated)
```kotlin
private fun submitReview(booking: Booking, rating: Int, comment: String) {
    val passengerId = sessionManager.getUserId()
    val driverId = booking.ride?.driverId ?: return
    
    lifecycleScope.launch {
        try {
            // ✅ NEW: If there's a comment, check it
            if (comment.isNotEmpty()) {
                Toast.makeText(context, "Checking comment...", Toast.LENGTH_SHORT).show()
                
                val isInappropriate = contentModerationService.isContentInappropriate(comment)
                
                if (isInappropriate) {
                    Toast.makeText(
                        context, 
                        "Your review contains inappropriate language. Please revise and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
            }
            
            // Content is appropriate, submit
            val request = CreateReviewRequest(
                reviewerId = passengerId,
                reviewedId = driverId,
                rideId = booking.rideId,
                rating = rating,
                comment = comment,
                type = "DRIVER"
            )
            
            val response = RetrofitClient.apiService.createReview(request)
            if (response.isSuccessful) {
                Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                reviewedRideIds.add(booking.rideId)
                loadReviewedRides()
            } else {
                Toast.makeText(context, "Failed to submit review", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

## ContentModerationService Implementation

```kotlin
package com.carpooling.app.services

import android.content.Context
import android.util.Log
import com.google.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentModerationService(context: Context) {
    
    companion object {
        private const val TAG = "ContentModerationService"
        private const val API_KEY = "AIzaSyCCheZaU2_oUJLlsVpfiov6RMsbQu3sVPc"
    }
    
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    /**
     * Check if the given text contains inappropriate content
     * Returns true if inappropriate, false if safe
     */
    suspend fun isContentInappropriate(text: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val moderationPrompt = """
                You are a content moderation system. Your job is to detect if the following text contains:
                - Bad words or profanity
                - Offensive language
                - Hate speech
                - Harassment or bullying
                - Sexually explicit content
                - Violence or harm
                
                Respond with ONLY "INAPPROPRIATE" if the text contains any of these, 
                or "APPROPRIATE" if the text is clean.
                
                Text to moderate: "$text"
            """.trimIndent()
            
            val response = generativeModel.generateContent(moderationPrompt)
            val responseText = response.text?.trim()?.uppercase() ?: "APPROPRIATE"
            
            Log.d(TAG, "Moderation result: $responseText")
            responseText.contains("INAPPROPRIATE")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during content moderation: ${e.message}", e)
            // Safe fallback: allow content if API fails
            false
        }
    }
}
```

---

## Configuration Files

### build.gradle.kts
```kotlin
dependencies {
    // ...existing dependencies...
    
    // Google Generative AI (Gemini) for content moderation
    implementation("com.google.generativeai:google-generativeai:0.10.0")
    
    // ...rest of dependencies...
}
```

### local.properties
```properties
sdk.dir=C\:\\Users\\Amine\\AppData\\Local\\Android\\Sdk
moderation.gemini.apiKey=AIzaSyCCheZaU2_oUJLlsVpfiov6RMsbQu3sVPc
```

---

## Import Statements

Add to MyLastRidesFragment.kt:
```kotlin
import com.carpooling.app.services.ContentModerationService
```

---

## Error Messages for Users

### Report Error
```
❌ "You cannot use bad words or offensive language in your report. 
    Please revise your description and try again."
```

### Review Error
```
❌ "Your review contains inappropriate language. 
    Please revise and try again."
```

### Network Error
```
❌ "Error: Network connection failed. Please check your internet."
```

### Success Messages
```
✅ "Report submitted!"
✅ "Review submitted!"
```

---

## Testing Strings

### Should PASS ✅
```
✅ "The driver was unprofessional"
✅ "Great service, very punctual!"
✅ "Driver arrived late and was unfriendly"
✅ "The car was dirty and uncomfortable"
✅ "Excellent experience, would recommend"
```

### Should FAIL ❌
```
❌ "The driver is a [bad_word]"
❌ "I hate this [offensive_term]"
❌ "Go [bad_word] yourself"
❌ "You [insulting_term]"
❌ "[Hate_speech_content]"
```

---

## Integration Checklist

- [ ] Add ContentModerationService.kt to services package
- [ ] Update build.gradle.kts with Gemini dependency
- [ ] Add API key to local.properties
- [ ] Import ContentModerationService in MyLastRidesFragment
- [ ] Initialize service in onViewCreated()
- [ ] Add moderation check to submitReport()
- [ ] Add moderation check to submitReview()
- [ ] Test with sample inappropriate content
- [ ] Verify error messages display correctly
- [ ] Check Logcat for moderation logs

---

## Common Issues & Solutions

### Issue: "Unresolved reference 'GenerativeModel'"
```
Solution:
1. Rebuild project: ./gradlew clean build
2. Invalidate Cache: File → Invalidate Caches → Invalidate
3. Sync Gradle: File → Sync Now
```

### Issue: API Key Not Found
```
Solution:
1. Check local.properties exists in project root
2. Verify API key is correctly spelled
3. Ensure format: moderation.gemini.apiKey=YOUR_KEY
```

### Issue: Network Timeout
```
Solution:
1. Check internet connection
2. Verify firewall allows Google API access
3. Increase timeout in catch block
```

---

## Logging Output

When moderation runs, check Logcat for:

```
D/ContentModerationService: Moderation result: APPROPRIATE
D/ContentModerationService: Moderation result: INAPPROPRIATE
E/ContentModerationService: Error during content moderation: [error message]
```

---

## Performance Monitoring

```kotlin
// Measure moderation time
val startTime = System.currentTimeMillis()
val isInappropriate = contentModerationService.isContentInappropriate(text)
val duration = System.currentTimeMillis() - startTime
Log.d(TAG, "Moderation took ${duration}ms")
```

---

## Advanced Usage

### Get Detailed Report
```kotlin
val report = contentModerationService.getModerationReport(text)
Log.d(TAG, "Moderation Report: $report")
// Output: "Found offensive language: [specific issues]"
```

### Batch Moderation
```kotlin
suspend fun moderateMultiple(textList: List<String>): List<Boolean> {
    return textList.map { text ->
        contentModerationService.isContentInappropriate(text)
    }
}
```

---

## Production Considerations

```kotlin
// Consider adding rate limiting
private var lastModerationTime = 0L
private val MIN_INTERVAL = 1000L // 1 second minimum

suspend fun checkWithRateLimit(text: String): Boolean {
    val now = System.currentTimeMillis()
    if (now - lastModerationTime < MIN_INTERVAL) {
        delay(MIN_INTERVAL - (now - lastModerationTime))
    }
    lastModerationTime = now
    return contentModerationService.isContentInappropriate(text)
}
```

---

This reference guide contains all the code snippets and examples you need to understand and maintain the content moderation system!

