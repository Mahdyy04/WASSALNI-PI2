import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { Report, ReportStatus, ReportReason } from '../models/report.model';
import { environment } from '../../../environments/environment';

/**
 * Backend Report interface
 */
interface BackendReport {
  id: number;
  reporterId: number;
  reportedUserId: number;
  rideId?: number;
  reason: string;
  description: string;
  status: string;
  createdAt: string;
  updatedAt?: string;
}

/**
 * Backend CreateReportRequest
 */
interface BackendCreateReportRequest {
  reporterId: number;
  reportedUserId: number;
  rideId?: number;
  reason: string;
  description: string;
}

/**
 * Report service - manages user reports
 */
@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = `${environment.apiUrl}${environment.services.reports}`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new report
   */
  createReport(reportData: Partial<Report>): Observable<Report> {
    const request: BackendCreateReportRequest = {
      reporterId: parseInt(reportData.reporterId || '0'),
      reportedUserId: parseInt(reportData.reportedUserId || '0'),
      rideId: reportData.rideId ? parseInt(reportData.rideId) : undefined,
      reason: reportData.reason || ReportReason.OTHER,
      description: reportData.description || ''
    };

    return this.http.post<BackendReport>(`${this.apiUrl}/create`, request).pipe(
      map(backendReport => this.mapBackendReportToFrontend(backendReport)),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to create report'));
      })
    );
  }

  /**
   * Get all reports (admin only)
   */
  getAllReports(): Observable<Report[]> {
    return this.http.get<BackendReport[]>(this.apiUrl).pipe(
      map(reports => reports.map(r => this.mapBackendReportToFrontend(r))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch reports'));
      })
    );
  }

  /**
   * Get reports by status
   */
  getReportsByStatus(status: ReportStatus): Observable<Report[]> {
    return this.http.get<BackendReport[]>(`${this.apiUrl}/status/${status}`).pipe(
      map(reports => reports.map(r => this.mapBackendReportToFrontend(r))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch reports'));
      })
    );
  }

  /**
   * Update report status (admin only)
   */
  updateReportStatus(reportId: string, status: ReportStatus): Observable<Report> {
    return this.http.put<BackendReport>(`${this.apiUrl}/${reportId}/status`, null, {
      params: new HttpParams().set('status', status)
    }).pipe(
      map(backendReport => this.mapBackendReportToFrontend(backendReport)),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to update report status'));
      })
    );
  }

  /**
   * Map backend report to frontend report
   */
  private mapBackendReportToFrontend(backendReport: BackendReport): Report {
    return {
      id: backendReport.id.toString(),
      reporterId: backendReport.reporterId.toString(),
      reporter: {} as any, // Will be populated separately if needed
      reportedUserId: backendReport.reportedUserId.toString(),
      reportedUser: {} as any, // Will be populated separately if needed
      reason: backendReport.reason as ReportReason,
      description: backendReport.description,
      status: backendReport.status as ReportStatus,
      rideId: backendReport.rideId?.toString(),
      createdAt: new Date(backendReport.createdAt)
    };
  }
}

