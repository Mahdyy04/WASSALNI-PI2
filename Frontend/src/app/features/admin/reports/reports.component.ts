import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ReportService } from '../../../core/services/report.service';
import { Report, ReportStatus } from '../../../core/models/report.model';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';

/**
 * Admin Reports Management component
 * Lists all reports and allows managing their status
 */
@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, DatePipe, CardComponent, ButtonComponent],
  template: `
    <div>
      <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-8">Reports Management</h1>
      
      <div class="space-y-4">
        @for (report of reports; track report.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-4 mb-4">
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                      Report #{{ report.id }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      Reported by: {{ report.reporter?.name || 'Unknown' }}
                    </p>
                  </div>
                  <span [class]="getStatusClass(report.status)">
                    {{ report.status }}
                  </span>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Reported User</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ report.reportedUser?.name || 'Unknown' }}
                    </p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Reason</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ report.reason }}
                    </p>
                  </div>
                </div>

                <div class="mb-4">
                  <p class="text-xs text-gray-500 dark:text-gray-400 mb-1">Description</p>
                  <p class="text-gray-900 dark:text-gray-100">{{ report.description }}</p>
                </div>

                <p class="text-xs text-gray-500 dark:text-gray-400">
                  Created: {{ report.createdAt | date:'short' }}
                </p>
              </div>

              @if (report.status === ReportStatus.PENDING) {
                <div class="ml-4 flex flex-col space-y-2">
                  <app-button
                    variant="primary"
                    size="sm"
                    (onClick)="updateStatus(report.id, ReportStatus.RESOLVED)"
                  >
                    Resolve
                  </app-button>
                  <app-button
                    variant="secondary"
                    size="sm"
                    (onClick)="updateStatus(report.id, ReportStatus.DISMISSED)"
                  >
                    Dismiss
                  </app-button>
                </div>
              }
            </div>
          </app-card>
        } @empty {
          <app-card>
            <div class="text-center py-12">
              <p class="text-gray-600 dark:text-gray-400">No reports found</p>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class AdminReportsComponent implements OnInit {
  reports: Report[] = [];
  ReportStatus = ReportStatus;

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.reportService.getAllReports().subscribe({
      next: (reports) => {
        this.reports = reports;
      },
      error: (error) => {
        console.error('Error loading reports:', error);
      }
    });
  }

  updateStatus(reportId: string, status: ReportStatus): void {
    this.reportService.updateReportStatus(reportId, status).subscribe({
      next: () => {
        this.loadReports();
      },
      error: (error) => {
        console.error('Error updating report status:', error);
      }
    });
  }

  getStatusClass(status: ReportStatus): string {
    const baseClasses = 'px-3 py-1 rounded-full text-sm font-medium';
    switch (status) {
      case ReportStatus.PENDING:
        return `${baseClasses} bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400`;
      case ReportStatus.RESOLVED:
        return `${baseClasses} bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400`;
      case ReportStatus.DISMISSED:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
      default:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
    }
  }
}

