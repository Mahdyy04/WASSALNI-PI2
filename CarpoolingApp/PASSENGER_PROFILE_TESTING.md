# Test Instructions for Passenger Profile Feature

## Prerequisites

1. Backend services must be running:
   - authentication-service (port 8081)
   - review-service (port 8086)
   - booking-service (port 8082)
   - ride-service (port 8085)

2. MongoDB must have test data inserted (see root README for MongoDB data)

3. Update `local.properties` with correct backend URL if needed

## Test Scenarios

### Scenario 1: View Passenger Profile with Reviews

**Steps:**
1. Login as driver:
   - Email: `john.driver@example.com`
   - Password: `password123`

2. Navigate to "Requests" tab (Pending Bookings)

3. You should see a pending booking from passenger: `507f1f77bcf86cd799439012`

4. Click "View Passenger Profile" button

**Expected Results:**
- Profile screen opens
- Passenger name is displayed (derived from email prefix)
- Email: `sarah.passenger@example.com`
- Phone number is shown
- Gender is displayed
- Average rating is shown (should be 4.8 from test data)
- Rating bar shows 4.8 stars
- Reviews section shows 1 review (if review data is present)

### Scenario 2: View Passenger Profile without Reviews

**Steps:**
1. Create a new passenger account without any reviews
2. Create a booking from this passenger
3. Login as driver
4. Navigate to pending bookings
5. Click "View Passenger Profile" for the new passenger

**Expected Results:**
- Profile screen opens
- All passenger information is displayed
- Average rating shows 0.0 or "N/A"
- Rating bar shows 0 stars
- Message "No reviews yet" is displayed

### Scenario 3: Network Error Handling

**Steps:**
1. Turn off backend services or disconnect from network
2. Login as driver (must be already cached)
3. Navigate to pending bookings
4. Click "View Passenger Profile"

**Expected Results:**
- Progress bar shows while trying to load
- Error toast message appears
- Activity closes gracefully and returns to bookings list

### Scenario 4: Invalid Passenger ID

**Steps:**
1. Modify code temporarily to pass invalid passenger ID
2. Try to open profile

**Expected Results:**
- Error toast: "Invalid passenger ID" or "Failed to load passenger profile"
- Activity closes and returns to previous screen

## Verification Checklist

- [ ] Profile loads successfully
- [ ] All user information displays correctly
- [ ] Average rating calculates and displays properly
- [ ] Rating bar visual matches numeric rating
- [ ] Reviews list populates if reviews exist
- [ ] Empty state shows when no reviews
- [ ] Back button in toolbar works
- [ ] Hardware back button works
- [ ] Loading indicator shows during data fetch
- [ ] Error messages are user-friendly
- [ ] Layout scrolls properly on small screens
- [ ] Material Design 3 styling is consistent
- [ ] Colors match app theme
- [ ] Icons are visible and appropriate
- [ ] Text is readable with good contrast
- [ ] Buttons are easily tappable

## API Response Examples

### Successful User Response:
```json
{
  "id": "507f1f77bcf86cd799439012",
  "email": "sarah.passenger@example.com",
  "phoneNumber": "+21698765432",
  "gender": "FEMALE",
  "userType": "PASSENGER",
  "isActive": true
}
```

### Successful Rating Response:
```json
{
  "userId": "507f1f77bcf86cd799439012",
  "averageRating": 4.8
}
```

### Successful Reviews Response:
```json
[
  {
    "id": "807f1f77bcf86cd799439042",
    "reviewerId": "507f1f77bcf86cd799439013",
    "reviewedId": "507f1f77bcf86cd799439012",
    "rideId": "607f1f77bcf86cd799439022",
    "rating": 5,
    "comment": "Great passenger! Very punctual.",
    "type": "PASSENGER"
  }
]
```

## Troubleshooting

### Profile doesn't load:
- Check backend services are running
- Verify network connectivity
- Check Logcat for error messages
- Verify passenger ID is valid

### Rating shows 0.0:
- Passenger may have no reviews yet (this is normal)
- Check if review-service is running
- Verify review data exists in MongoDB

### Reviews don't appear:
- Check if reviews exist for this passenger with type "PASSENGER"
- Verify review-service endpoint is correct
- Check API response in Logcat

### Layout issues:
- Clear app cache and rebuild
- Check if all layout files are properly synced
- Verify Material3 dependencies in build.gradle

## Debug Commands

### Check if backend is accessible:
```bash
curl http://your-backend-url:8081/authentication-service/api/auth/users/507f1f77bcf86cd799439012
```

### Check MongoDB data:
```javascript
db.users.findOne({_id: ObjectId("507f1f77bcf86cd799439012")})
db.reviews.find({reviewedId: "507f1f77bcf86cd799439012", type: "PASSENGER"})
```

### Check Android logs:
```bash
adb logcat | grep -i "PassengerProfile"
```

## Performance Testing

1. **Load Time:**
   - Profile should load within 2-3 seconds on good network
   - Progress bar should be visible during load

2. **Scroll Performance:**
   - Review list should scroll smoothly
   - No lag when scrolling through many reviews

3. **Memory Usage:**
   - Monitor memory usage in Android Profiler
   - No memory leaks when opening/closing profile multiple times

## Edge Cases to Test

1. Very long passenger names
2. Missing phone number
3. Missing gender
4. Special characters in email
5. Very long review comments
6. Passenger with 100+ reviews
7. Rating exactly 0.0
8. Rating exactly 5.0
9. Fractional ratings (e.g., 3.7, 4.2)

## Automated Testing (Future)

Consider adding:
- Unit tests for ReviewAdapter
- UI tests for PassengerProfileActivity
- Integration tests for API calls
- Mock server responses for testing

---

**Last Updated:** 2025-12-02
**Feature Version:** 1.0

