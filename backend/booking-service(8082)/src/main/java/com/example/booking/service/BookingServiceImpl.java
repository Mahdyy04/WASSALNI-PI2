package com.example.booking.service;

import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.dto.BookingResponse;
import com.example.booking.entities.Booking;
import com.example.booking.enums.BookingStatus;
import com.example.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public BookingResponse bookRide(CreateBookingRequest request) {

        Booking booking = new Booking();
        booking.setRideId(request.getRideId());
        booking.setPassengerId(request.getPassengerId());
        // Ensure seats is at least 1
        int seats = (request.getSeats() != null && request.getSeats() > 0) ? request.getSeats() : 1;
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        // Get ride info to find the driver and create notification
        try {
            Map<String, Object> rideInfo = webClientBuilder.build()
                    .get()
                    .uri("http://ride-service/api/rides/{rideId}", request.getRideId())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (rideInfo != null) {
                String driverId = (String) rideInfo.get("driverId");
                Map<String, Object> departureCity = (Map<String, Object>) rideInfo.get("departureCity");
                Map<String, Object> destinationCity = (Map<String, Object>) rideInfo.get("destinationCity");
                String from = departureCity != null ? (String) departureCity.get("name") : "Unknown";
                String to = destinationCity != null ? (String) destinationCity.get("name") : "Unknown";
                
                // Send notification to driver
                sendNotification(
                        driverId,
                        "BOOKING_REQUEST",
                        "New Booking Request",
                        "A passenger has requested to book " + seats + " seat(s) for your ride from " + from + " to " + to,
                        request.getRideId(),
                        saved.getId()
                );
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not send booking notification: " + e.getMessage());
        }

        BookingResponse response = new BookingResponse();
        response.setBookingId(saved.getId());
        response.setRideId(saved.getRideId());
        response.setPassengerId(saved.getPassengerId());
        response.setSeatsBooked(saved.getSeatsBooked());
        response.setStatus(saved.getStatus());

        return response;
    }

    @Override
    public void cancelBooking(String bookingId, String passengerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPassengerId().equals(passengerId)) {
            throw new RuntimeException("You cannot cancel another user's booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    @Override
    public void acceptBooking(String bookingId, String driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        int seatsToDeduct = booking.getSeatsBooked() != null ? booking.getSeatsBooked() : 1;
        
        // Update booking status
        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Update available seats in ride service and get ride info for notification
        String from = "Unknown";
        String to = "Unknown";
        try {
            System.out.println("Updating ride seats: rideId=" + booking.getRideId() + ", seatsToDeduct=" + seatsToDeduct);
            
            // First, get ride info
            Map<String, Object> rideInfo = webClientBuilder.build()
                    .get()
                    .uri("http://ride-service/api/rides/{rideId}", booking.getRideId())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (rideInfo != null) {
                Map<String, Object> departureCity = (Map<String, Object>) rideInfo.get("departureCity");
                Map<String, Object> destinationCity = (Map<String, Object>) rideInfo.get("destinationCity");
                from = departureCity != null ? (String) departureCity.get("name") : "Unknown";
                to = destinationCity != null ? (String) destinationCity.get("name") : "Unknown";
            }
            
            // Then update seats
            webClientBuilder.build()
                    .put()
                    .uri("http://ride-service/api/rides/{rideId}/seats?seatsToDeduct={seats}",
                            booking.getRideId(), seatsToDeduct)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            System.out.println("Successfully updated ride seats");
        } catch (Exception e) {
            // Log error but don't fail the booking acceptance
            System.err.println("Warning: Could not update ride seats: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Send notification to passenger
        try {
            sendNotification(
                    booking.getPassengerId(),
                    "BOOKING_ACCEPTED",
                    "Booking Accepted!",
                    "Your booking for the ride from " + from + " to " + to + " has been accepted by the driver.",
                    booking.getRideId(),
                    bookingId
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not send acceptance notification: " + e.getMessage());
        }
    }

    @Override
    public void rejectBooking(String bookingId, String driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // TODO: Verify driver owns the ride
        // This would require calling ride-service to check ride ownership

        booking.setStatus(BookingStatus.REJECTED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        
        // Get ride info for notification
        String from = "Unknown";
        String to = "Unknown";
        try {
            Map<String, Object> rideInfo = webClientBuilder.build()
                    .get()
                    .uri("http://ride-service/api/rides/{rideId}", booking.getRideId())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (rideInfo != null) {
                Map<String, Object> departureCity = (Map<String, Object>) rideInfo.get("departureCity");
                Map<String, Object> destinationCity = (Map<String, Object>) rideInfo.get("destinationCity");
                from = departureCity != null ? (String) departureCity.get("name") : "Unknown";
                to = destinationCity != null ? (String) destinationCity.get("name") : "Unknown";
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not get ride info: " + e.getMessage());
        }
        
        // Send notification to passenger
        try {
            sendNotification(
                    booking.getPassengerId(),
                    "BOOKING_REJECTED",
                    "Booking Rejected",
                    "Your booking for the ride from " + from + " to " + to + " has been rejected by the driver.",
                    booking.getRideId(),
                    bookingId
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not send rejection notification: " + e.getMessage());
        }
    }

    @Override
    public List<Booking> getBookingsByPassenger(String passengerId) {
        return bookingRepository.findByPassengerId(passengerId);
    }

    @Override
    public List<Booking> getBookingsByRide(String rideId) {
        return bookingRepository.findByRideId(rideId);
    }

    @Override
public List<Booking> getPendingBookingsByDriver(String driverId) {
    // Pour l'instant, retourne tous les bookings PENDING
    // En production, il faudrait appeler ride-service pour filtrer par driver
    return bookingRepository.findAll().stream()
            .filter(b -> b.getStatus() == BookingStatus.PENDING)
            .collect(Collectors.toList());
}

    /**
     * Helper method to send notifications via notification-service
     */
    private void sendNotification(String userId, String type, String title, String message, String rideId, String bookingId) {
        try {
            Map<String, String> notificationRequest = new HashMap<>();
            notificationRequest.put("userId", userId);
            notificationRequest.put("type", type);
            notificationRequest.put("title", title);
            notificationRequest.put("message", message);
            notificationRequest.put("rideId", rideId);
            notificationRequest.put("bookingId", bookingId);
            
            webClientBuilder.build()
                    .post()
                    .uri("http://notification-service/api/notifications/create")
                    .bodyValue(notificationRequest)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .block();
            
            System.out.println("Notification sent successfully to user: " + userId);
        } catch (Exception e) {
            System.err.println("Warning: Could not send notification: " + e.getMessage());
        }
    }
}