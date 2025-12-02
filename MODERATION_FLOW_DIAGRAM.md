# Content Moderation Flow Diagram

## Report Submission Flow

```
┌─────────────────────────────────────────┐
│  User Opens "Report" Dialog             │
│  (MyLastRidesFragment.kt)               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  User Fills Form:                       │
│  - Select Reason                        │
│  - Enter Description                    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  User Clicks "Submit Report"            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Toast: "Checking content..."           │
│  (Show loading indicator)               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  ContentModerationService               │
│  .isContentInappropriate(description)   │
│                                         │
│  Calls Gemini API with prompt           │
└──────────────┬──────────────────────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
    INAPPROPRIATE  APPROPRIATE
         │           │
         │           ▼
         │    ┌──────────────────────────┐
         │    │ Create CreateReportRequest
         │    │ (reporterId, reportedId) │
         │    └──────────────┬───────────┘
         │                   │
         │                   ▼
         │    ┌──────────────────────────┐
         │    │ Submit to Backend API    │
         │    │ RetrofitClient           │
         │    └──────────────┬───────────┘
         │                   │
         │                   ▼
         │    ┌──────────────────────────┐
         │    │ Success Toast:           │
         │    │ "Report submitted!"      │
         │    └──────────────────────────┘
         │
         ▼
    ┌──────────────────────────────────────────┐
    │ Show Error Toast:                        │
    │ "You cannot use bad words or offensive  │
    │  language in your report. Please revise │
    │  your description and try again."       │
    │                                          │
    │ Report NOT submitted                    │
    └──────────────────────────────────────────┘
```

## Review Submission Flow

```
┌─────────────────────────────────────────┐
│  User Opens "Write Review" Dialog       │
│  (MyLastRidesFragment.kt)               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  User Fills Form:                       │
│  - Select Rating (1-5 stars)            │
│  - Enter Comment (optional)             │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  User Clicks "Submit Review"            │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  If comment is empty:                   │
│  ✓ Skip moderation, proceed            │
│                                         │
│  If comment has content:                │
│  ↓ Check content...                    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Toast: "Checking comment..."           │
│  (Show loading indicator)               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  ContentModerationService               │
│  .isContentInappropriate(comment)       │
│                                         │
│  Calls Gemini API with prompt           │
└──────────────┬──────────────────────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
    INAPPROPRIATE  APPROPRIATE
         │           │
         │           ▼
         │    ┌──────────────────────────┐
         │    │ Create CreateReviewRequest
         │    │ (reviewerId, rating,     │
         │    │  comment, type)          │
         │    └──────────────┬───────────┘
         │                   │
         │                   ▼
         │    ┌──────────────────────────┐
         │    │ Submit to Backend API    │
         │    │ RetrofitClient           │
         │    └──────────────┬───────────┘
         │                   │
         │                   ▼
         │    ┌──────────────────────────┐
         │    │ Success Toast:           │
         │    │ "Review submitted!"      │
         │    │                          │
         │    │ Refresh ride list        │
         │    └──────────────────────────┘
         │
         ▼
    ┌──────────────────────────────────────────┐
    │ Show Error Toast:                        │
    │ "Your review contains inappropriate     │
    │  language. Please revise and try again."│
    │                                          │
    │ Review NOT submitted                    │
    └──────────────────────────────────────────┘
```

## API Request Flow

```
┌──────────────────────────────────────────┐
│  ContentModerationService                │
│  isContentInappropriate(text)            │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│  Build Moderation Prompt                 │
│                                          │
│  "You are a content moderation system.  │
│   Detect bad words, offensive language,  │
│   hate speech, harassment, explicit     │
│   content, violence.                    │
│                                          │
│   Respond with ONLY:                    │
│   INAPPROPRIATE or APPROPRIATE          │
│                                          │
│   Text: [user's text]"                  │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│  Call Gemini API                         │
│  Model: gemini-1.5-flash                 │
│  apiKey: AIzaSyCCheZa...                │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│  API Response:                           │
│  "INAPPROPRIATE" or "APPROPRIATE"        │
└──────────────┬───────────────────────────┘
               │
         ┌─────┴─────┐
         │           │
         ▼           ▼
    BLOCK      ALLOW
  Submission  Submission
```

## Error Handling Flow

```
┌────────────────────────────────────────┐
│  Content Moderation Check              │
└──────────────┬─────────────────────────┘
               │
         ┌─────┴──────┬─────────────────┐
         │            │                 │
         ▼            ▼                 ▼
    SUCCESS       EXCEPTION        API DOWN
         │            │                 │
         ▼            ▼                 ▼
    Process     Log Error          Default
    Content   Show Network       to Allow
             Error Toast       (Safe Mode)
             Exception:        │
             IOException       │
             TimeoutException  │
             etc.              ▼
                           Continue
                           Submission
```

## Key Components

### 1. Service Layer
```
ContentModerationService
├── isContentInappropriate(text)
├── getModerationReport(text)
└── Uses Gemini API
```

### 2. UI Layer
```
MyLastRidesFragment
├── submitReport()
│   └── Check description → Block/Allow
└── submitReview()
    └── Check comment → Block/Allow
```

### 3. API Communication
```
Gemini API
└── Analyzes text
    └── Returns APPROPRIATE/INAPPROPRIATE
```

## Response Times

```
Typical Flow:
├── Show checking toast: Immediate
├── Send to Gemini: ~500ms
├── Receive response: ~1s
├── Process result: <100ms
└── Show result to user: Immediate

Total: 1-1.5 seconds
```

## Success Criteria

✅ Report with clean content → Submitted successfully
✅ Report with bad words → Blocked with error message
✅ Review with clean comment → Submitted successfully
✅ Review with offensive language → Blocked with error message
✅ Empty review comment → Allowed (optional field)
✅ API timeout → Gracefully allowed (fail-safe mode)

---

This moderation system ensures that all user-generated content (reports and reviews) is screened before submission to maintain a safe and respectful community.

