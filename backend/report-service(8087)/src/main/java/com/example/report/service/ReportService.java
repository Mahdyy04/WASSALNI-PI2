package com.example.report.service;

import com.example.report.dto.CreateReportRequest;
import com.example.report.entities.Report;

import java.util.List;

public interface ReportService {
    Report createReport(CreateReportRequest request);
    List<Report> getAllReports();
    List<Report> getReportsByStatus(Report.ReportStatus status);
    List<Report> getReportsByReporter(String reporterId);
    List<Report> getReportsByReportedUser(String reportedUserId);
    Report updateReportStatus(String reportId, Report.ReportStatus status);
    Report getReportById(String reportId);
}