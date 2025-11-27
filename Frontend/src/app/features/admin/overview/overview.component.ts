import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { RideService } from '../../../core/services/ride.service';
import { ReportService } from '../../../core/services/report.service';
import { ReportStatus } from '../../../core/models/report.model';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Admin Overview component
 * Dashboard overview with statistics
 */
@Component({
  selector: 'app-admin-overview',
  standalone: true,
  imports: [CommonModule, CardComponent],
  template: `
    <div>
      <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-8">Dashboard Overview</h1>
      
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Total Users</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-gray-100">{{ totalUsers }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
            </div>
          </div>
        </app-card>

        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Total Rides</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-gray-100">{{ totalRides }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
              </svg>
            </div>
          </div>
        </app-card>

        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Pending Reports</p>
              <p class="text-3xl font-bold text-gray-900 dark:text-gray-100">{{ pendingReports }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-red-600 dark:text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
          </div>
        </app-card>
      </div>
    </div>
  `,
  styles: []
})
export class AdminOverviewComponent implements OnInit {
  totalUsers = 0;
  totalRides = 0;
  pendingReports = 0;

  constructor(
    private authService: AuthService,
    private rideService: RideService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    // Load users count
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        this.totalUsers = users.length;
      },
      error: (error) => {
        console.error('Error loading users:', error);
      }
    });

    // Load rides count
    this.rideService.getAllRides().subscribe({
      next: (rides) => {
        this.totalRides = rides.length;
      },
      error: (error) => {
        console.error('Error loading rides:', error);
      }
    });

    // Load pending reports count
    this.reportService.getReportsByStatus(ReportStatus.PENDING).subscribe({
      next: (reports) => {
        this.pendingReports = reports.length;
      },
      error: (error) => {
        console.error('Error loading reports:', error);
      }
    });
  }
}

