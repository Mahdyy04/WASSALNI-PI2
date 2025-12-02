# Passenger Profile View - Implementation Guide

## Overview
A new passenger profile view has been added to the driver portal, allowing drivers to view detailed information about passengers including their ratings, reviews, and personal information before accepting booking requests.

## Files Created

### 1. PassengerProfileActivity.kt
**Location:** `app/src/main/java/com/carpooling/app/PassengerProfileActivity.kt`

This activity displays the passenger's complete profile with:
- Personal information (name, email, phone, gender)
- Average rating as a passenger
- List of reviews received as a passenger
- User type badge

**Features:**
- Loads passenger data from backend API
- Displays average rating with rating bar
- Shows all passenger reviews in a scrollable list
- Handles loading states with progress bar
- Graceful error handling

### 2. ReviewAdapter.kt
**Location:** `app/src/main/java/com/carpooling/app/adapters/ReviewAdapter.kt`

RecyclerView adapter for displaying reviews with:
- Star rating visualization
- Review comment text
- Reviewer information
- Ride reference

### 3. Layout Files

#### activity_passenger_profile.xml
**Location:** `app/src/main/res/layout/activity_passenger_profile.xml`

Main layout for the passenger profile screen with:
- Toolbar with back button
- Profile information card with icons
- Rating display card with large rating bar
- Reviews section with RecyclerView
- Empty state for when there are no reviews

#### item_review.xml
**Location:** `app/src/main/res/layout/item_review.xml`

Layout for individual review items showing:
- Reviewer name
- Star rating (small and compact)
- Review comment
- Ride reference

## Files Modified

### 1. PendingBookingAdapter.kt
**Changes:**
- Added `onViewProfileClick` callback parameter
- Added click listener for "View Profile" button
- Handles navigation to passenger profile

### 2. PendingBookingsFragment.kt
**Changes:**
- Added `viewPassengerProfile()` method
- Updated adapter initialization to include profile view callback
- Launches PassengerProfileActivity with passenger ID

### 3. item_pending_booking.xml
**Changes:**
- Added "View Passenger Profile" button
- Button positioned between price and action buttons
- Uses Material3 TonalButton style with info icon

### 4. AndroidManifest.xml
**Changes:**
- Registered PassengerProfileActivity

### 5. colors.xml
**Changes:**
- Added `text_hint` color (#999999) for subtle text

## How It Works

### Driver Flow:

1. **View Pending Bookings**
   - Driver navigates to "Requests" tab in dashboard
   - Sees list of pending booking requests

2. **View Passenger Profile**
   - Driver clicks "View Passenger Profile" button on any booking
   - PassengerProfileActivity is launched

3. **Review Passenger Information**
   - Driver sees passenger's:
     - Display name (derived from email)
     - Email address
     - Phone number
     - Gender
     - User type badge
     - Average rating as passenger (0-5 stars)
     - Number of reviews received
     - Full list of reviews with comments

4. **Make Informed Decision**
   - Based on profile information and ratings, driver can:
     - Go back and accept the booking
     - Go back and reject the booking

### API Endpoints Used:

```kotlin
// Get user information
GET /authentication-service/api/auth/users/{userId}

// Get average rating
GET /review-service/api/reviews/user/{userId}/average

// Get passenger reviews
GET /review-service/api/reviews/user/{userId}/type/PASSENGER
```

## Testing

### To test the feature:

1. **Login as Driver:**
   ```
   Email: john.driver@example.com
   Password: password123
   ```

2. **Navigate to Requests Tab**
   - You should see pending booking requests

3. **Click "View Passenger Profile"**
   - Profile screen will load passenger information

4. **Verify Display:**
   - Check that passenger info is displayed correctly
   - Verify rating bar shows correct value
   - Scroll through reviews if available

### Sample Data:

Using the MongoDB data provided earlier, you can test with:
- Passenger ID: `507f1f77bcf86cd799439012` (sarah.passenger@example.com)
- Passenger ID: `507f1f77bcf86cd799439014` (fatma.passenger@example.com)

## UI/UX Features

### Material Design 3:
- Uses Material3 components (MaterialButton, MaterialCardView)
- Consistent elevation and corner radius
- Primary color theming

### Visual Hierarchy:
- Large passenger name at top
- User type badge for quick identification
- Icons for contact information
- Prominent rating display
- Clear review cards

### Loading States:
- Progress bar during data fetch
- Graceful handling of missing data
- Empty state message when no reviews

### Error Handling:
- Toast messages for errors
- Automatic activity close on critical errors
- Network error handling

## Future Enhancements

Potential improvements:
1. Add passenger profile photo
2. Show verified badge for verified users
3. Display number of completed rides
4. Add filters for review types
5. Show booking history with this passenger
6. Add direct messaging capability
7. Show mutual connections/reviews
8. Export profile or reviews as PDF

## Backend Requirements

The feature requires these API endpoints to be implemented:
- User service must return user details by ID
- Review service must calculate and return average ratings
- Review service must filter reviews by user and type (PASSENGER/DRIVER)

## Security Considerations

- Passenger ID is passed via Intent extras
- Only authenticated drivers can access profile
- Profile data is fetched from secure API endpoints
- No sensitive information (passwords, payment details) displayed
- HTTPS required for API communication

## Performance

- Async data loading with Kotlin Coroutines
- RecyclerView for efficient review list rendering
- Nested scroll view for smooth scrolling
- Image and data caching recommended for future versions

## Accessibility

- Proper content descriptions needed (future enhancement)
- Clear text labels and sizing
- Good color contrast ratios
- Tappable areas meet minimum size requirements

---

## Summary

The passenger profile view empowers drivers to make informed decisions about booking requests by providing comprehensive information about passengers, including their reputation and history as rated by other drivers. This feature enhances safety and trust in the carpooling platform.

