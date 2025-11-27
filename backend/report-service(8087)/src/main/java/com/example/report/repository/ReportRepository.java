package com.example.report.repository;

import com.example.report.entities.Report;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByReporterId(String reporterId);
    List<Report> findByReportedUserId(String reportedUserId);
    List<Report> findByStatus(Report.ReportStatus status);
    List<Report> findByRideId(String rideId);
}
