package com.example.report.service;

import com.example.report.dto.CreateReportRequest;
import com.example.report.entities.Report;
import com.example.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    public Report createReport(CreateReportRequest request) {
        Report report = new Report();
        report.setReporterId(request.getReporterId());
        report.setReportedUserId(request.getReportedUserId());
        report.setRideId(request.getRideId());
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus(Report.ReportStatus.PENDING);
        return reportRepository.save(report);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatus(status);
    }

    @Override
    public List<Report> getReportsByReporter(String reporterId) {
        return reportRepository.findByReporterId(reporterId);
    }

    @Override
    public List<Report> getReportsByReportedUser(String reportedUserId) {
        return reportRepository.findByReportedUserId(reportedUserId);
    }

    @Override
    public Report updateReportStatus(String reportId, Report.ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    @Override
    public Report getReportById(String reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }
}