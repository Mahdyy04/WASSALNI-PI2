package com.example.report.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reports") // MongoDB collection name
public class Report {

    @Id
    private String id;

    private String reporterId;
    private String reportedUserId;
    private String rideId;

    private ReportReason reason;
    private String description;

    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public enum ReportReason {
        INAPPROPRIATE_BEHAVIOR,
        NO_SHOW,
        UNSAFE_DRIVING,
        OTHER
    }

    public enum ReportStatus {
        PENDING,
        REVIEWED,
        RESOLVED,
        DISMISSED
    }
}
