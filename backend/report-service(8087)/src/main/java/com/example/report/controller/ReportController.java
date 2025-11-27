package com.example.report.controller;

import com.example.report.dto.CreateReportRequest;
import com.example.report.entities.Report;
import com.example.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create")
    public Report createReport(@RequestBody CreateReportRequest request) {
        return reportService.createReport(request);
    }

    @GetMapping
    public List<Report> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/status/{status}")
    public List<Report> getReportsByStatus(@PathVariable Report.ReportStatus status) {
        return reportService.getReportsByStatus(status);
    }

    @GetMapping("/reporter/{reporterId}")
    public List<Report> getReportsByReporter(@PathVariable String reporterId) {
        return reportService.getReportsByReporter(reporterId);
    }

    @GetMapping("/reported/{reportedUserId}")
    public List<Report> getReportsByReportedUser(@PathVariable String reportedUserId) {
        return reportService.getReportsByReportedUser(reportedUserId);
    }

    @GetMapping("/{reportId}")
    public Report getReportById(@PathVariable String reportId) {
        return reportService.getReportById(reportId);
    }

    @PutMapping("/{reportId}/status")
    public Report updateReportStatus(
            @PathVariable String reportId,
            @RequestParam Report.ReportStatus status) {
        return reportService.updateReportStatus(reportId, status);
    }
}