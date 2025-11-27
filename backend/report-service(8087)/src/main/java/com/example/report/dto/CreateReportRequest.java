package com.example.report.dto;

import com.example.report.entities.Report;
import lombok.Data;

@Data
public class CreateReportRequest {
    private String reporterId;
    private String reportedUserId;
    private String rideId;
    private Report.ReportReason reason;
    private String description;
}