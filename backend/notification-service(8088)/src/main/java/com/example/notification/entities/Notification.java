package com.example.notification.entities;

import com.example.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private String userId;           // User who receives the notification
    private NotificationType type;
    private String title;
    private String message;
    private String rideId;           // Related ride
    private String bookingId;        // Related booking
    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}
