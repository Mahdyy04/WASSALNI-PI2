# Content Moderation Implementation Guide

## Overview
This document describes the implementation of content moderation using Google Generative AI (Gemini API) in the Carpooling application.

## Features Implemented

### 1. **Gemini API Integration**
- **API Key**: `AIzaSyCCheZaU2_oUJLlsVpfiov6RMsbQu3sVPc`
- **Location**: Stored in `local.properties` file
- **Model**: `gemini-1.5-flash` for fast content moderation

### 2. **Content Moderation Service**
- **File**: `ContentModerationService.kt`
- **Location**: `app/src/main/java/com/carpooling/app/services/`
- **Functions**:
  - `isContentInappropriate()` - Returns true if content contains bad words/offensive language
  - `getModerationReport()` - Returns detailed analysis of inappropriate content

### 3. **Moderation Triggers**

#### Report Submissions
- **Location**: `MyLastRidesFragment.kt` - `submitReport()` function
- **Behavior**:
  - When user tries to submit a report, the description is checked
  - If inappropriate content is detected, shows error: "You cannot use bad words or offensive language in your report. Please revise your description and try again."
  - Only safe content is submitted to the backend

#### Review Comments
- **Location**: `MyLastRidesFragment.kt` - `submitReview()` function
- **Behavior**:
  - When user submits a review with a comment, the comment is checked
  - If inappropriate content is detected, shows error: "Your review contains inappropriate language. Please revise and try again."
  - Only safe reviews are submitted to the backend

## Content Categories Detected

The Gemini API checks for:
- ✅ Bad words and profanity
- ✅ Offensive language
- ✅ Hate speech
- ✅ Harassment or bullying
- ✅ Sexually explicit content
- ✅ Violence or harm

## Setup Instructions

### 1. Add Dependencies to `build.gradle.kts`
```kotlin
implementation("com.google.generativeai:google-generativeai:0.10.0")
```

### 2. Add API Key to `local.properties`
```properties
moderation.gemini.apiKey=AIzaSyCCheZaU2_oUJLlsVpfiov6RMsbQu3sVPc
```

### 3. Initialize ContentModerationService
```kotlin
private lateinit var contentModerationService: ContentModerationService

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    contentModerationService = ContentModerationService(requireContext())
    // ...
}
```

## Usage Examples

### Example 1: Check Report Description
```kotlin
val isInappropriate = contentModerationService.isContentInappropriate(description)
if (isInappropriate) {
    Toast.makeText(
        context, 
        "You cannot use bad words or offensive language.",
        Toast.LENGTH_LONG
    ).show()
    return@launch
}
// Proceed with submission
```

### Example 2: Check Review Comment
```kotlin
if (comment.isNotEmpty()) {
    val isInappropriate = contentModerationService.isContentInappropriate(comment)
    if (isInappropriate) {
        Toast.makeText(
            context, 
            "Your review contains inappropriate language.",
            Toast.LENGTH_LONG
        ).show()
        return@launch
    }
}
// Proceed with submission
```

## Error Handling

- **API Failures**: If the Gemini API fails, the content is allowed to proceed (default safe mode)
- **Network Errors**: Caught and logged, displays network error toast
- **Empty Content**: Empty comments in reviews are allowed (optional field)

## Logging

- **Tag**: `ContentModerationService`
- **Output**: Moderation results and errors logged to Logcat
- **Example**:
  ```
  D/ContentModerationService: Moderation result: INAPPROPRIATE
  D/ContentModerationService: Moderation result: APPROPRIATE
  ```

## API Response Flow

1. User submits report or review with content
2. Content is sent to Gemini API
3. API analyzes content against moderation criteria
4. Response is "INAPPROPRIATE" or "APPROPRIATE"
5. Based on response:
   - **INAPPROPRIATE**: Show error message, block submission
   - **APPROPRIATE**: Proceed with normal API submission

## Performance Considerations

- Uses `Dispatchers.IO` for API calls (non-blocking)
- Lightweight API calls using Gemini Flash model
- Minimal latency due to model choice
- Graceful degradation if API is unavailable

## Testing

### Test Case 1: Safe Report
```
Input: "The driver was rude during the journey"
Expected: Allowed ✅
```

### Test Case 2: Inappropriate Report
```
Input: "The driver is a [bad word]"
Expected: Blocked with error message ❌
```

### Test Case 3: Safe Review
```
Input: "Great driver, very professional!"
Expected: Allowed ✅
```

### Test Case 4: Offensive Review
```
Input: "Hate this driver, [offensive language]"
Expected: Blocked with error message ❌
```

## Security Notes

- ⚠️ **API Key Visibility**: Store in `local.properties` (NOT committed to version control)
- ✅ **Data Privacy**: Content is sent to Google Generative AI for analysis
- ✅ **User Privacy**: API only sees submitted content, no persistent storage
- ✅ **Rate Limiting**: Consider implementing rate limiting for production

## Future Enhancements

1. **Localization**: Multi-language moderation support
2. **Custom Dictionary**: Add app-specific terms to block
3. **Analytics**: Track moderation events
4. **Admin Controls**: Configure severity levels
5. **User Appeal**: Allow users to dispute blocked content
6. **Caching**: Cache moderation results for identical content

## Troubleshooting

### Issue: "Unresolved reference 'GenerativeModel'"
**Solution**: Ensure gradle dependency is correctly added and project is rebuilt

### Issue: API Key Error
**Solution**: Verify API key in `local.properties` and enable Generative AI API in Google Cloud Console

### Issue: Network Error
**Solution**: Check internet connection and firewall settings

## File References

- **Service**: `app/src/main/java/com/carpooling/app/services/ContentModerationService.kt`
- **Fragment**: `app/src/main/java/com/carpooling/app/fragments/MyLastRidesFragment.kt`
- **Build Config**: `app/build.gradle.kts`
- **Local Config**: `local.properties`

## API Documentation

For more information, visit:
- Google Generative AI: https://ai.google.dev
- Gemini API: https://ai.google.dev/tutorials/java_quickstart

