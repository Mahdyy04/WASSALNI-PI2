import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { ReportService } from '../../../core/services/report.service';
import { ReportReason } from '../../../core/models/report.model';
import { AuthService } from '../../../core/services/auth.service';
import { User } from '../../../core/models/user.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Create Report component
 * Allows users to report problems with other users
 */
@Component({
  selector: 'app-create-report',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonComponent,
    CardComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-8 max-w-2xl">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Report a Problem</h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Help us maintain a safe and respectful community
        </p>
      </div>

      <app-card>
        <form [formGroup]="reportForm" (ngSubmit)="onSubmit()">
          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Select User <span class="text-red-500">*</span>
            </label>
            <select formControlName="reportedUserId" class="input-field">
              <option value="">-- Select a user --</option>
              <option *ngFor="let user of availableUsers" [value]="user.id">
                {{ user.name }} ({{ user.email }})
              </option>
            </select>
            <p *ngIf="getErrorMessage('reportedUserId')" class="mt-1 text-sm text-red-600 dark:text-red-400">
              {{ getErrorMessage('reportedUserId') }}
            </p>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Reason <span class="text-red-500">*</span>
            </label>
            <select formControlName="reason" class="input-field">
              <option value="">-- Select a reason --</option>
              <option *ngFor="let reason of reportReasons" [value]="reason.value">{{ reason.label }}</option>
            </select>
            <p *ngIf="getErrorMessage('reason')" class="mt-1 text-sm text-red-600 dark:text-red-400">
              {{ getErrorMessage('reason') }}
            </p>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Description <span class="text-red-500">*</span>
            </label>
            <textarea
                formControlName="description"
                rows="5"
                class="input-field"
                placeholder="Please provide details about the issue..."
            ></textarea>
            <p *ngIf="getErrorMessage('description')" class="mt-1 text-sm text-red-600 dark:text-red-400">
              {{ getErrorMessage('description') }}
            </p>
          </div>

          <div *ngIf="errorMessage" class="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
            <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
          </div>

          <div *ngIf="successMessage" class="mb-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
            <p class="text-sm text-green-600 dark:text-green-400">{{ successMessage }}</p>
          </div>

          <div class="flex space-x-4">
            <app-button type="submit" variant="primary" size="lg" [loading]="loading">Submit Report</app-button>
            <app-button type="button" variant="secondary" size="lg" (onClick)="cancel()">Cancel</app-button>
          </div>
        </form>
      </app-card>
    </div>
  `,
  styles: []
})
export class CreateReportComponent implements OnInit {
  reportForm: FormGroup;
  availableUsers: User[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  reportReasons = [
    { value: ReportReason.INAPPROPRIATE_BEHAVIOR, label: 'Inappropriate Behavior' },
    { value: ReportReason.NO_SHOW, label: 'No Show' },
    { value: ReportReason.UNSAFE_DRIVING, label: 'Unsafe Driving' },
    { value: ReportReason.OTHER, label: 'Other' }
  ];

  constructor(
      private fb: FormBuilder,
      private authStore: AuthStore,
      private reportService: ReportService,
      private authService: AuthService,
      private router: Router
  ) {
    this.reportForm = this.fb.group({
      reportedUserId: ['', Validators.required],
      reason: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  ngOnInit(): void {
    this.loadAvailableUsers();
  }

  loadAvailableUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (users: User[]) => {
        const currentUser = this.authStore.currentUser();
        this.availableUsers = users.filter(u => u.id !== currentUser?.id);
      },
      error: (error: any) => {
        console.error('Error loading users:', error);
        this.errorMessage = 'Failed to load users. Please try again.';
      }
    });
  }

  onSubmit(): void {
    if (this.reportForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.reportForm.value;
    const currentUser = this.authStore.currentUser();
    const reportedUser = this.availableUsers.find(u => u.id === formValue.reportedUserId);

    if (!currentUser || !reportedUser) {
      this.errorMessage = 'Invalid user selection';
      this.loading = false;
      return;
    }

    const reportData = {
      reporterId: currentUser.id,
      reporter: currentUser,
      reportedUserId: reportedUser.id,
      reportedUser,
      reason: formValue.reason,
      description: formValue.description
    };

    this.reportService.createReport(reportData).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Report submitted successfully.';
        setTimeout(() => this.router.navigate(['/']), 2000);
      },
      error: (error: any) => {
        this.loading = false;
        this.errorMessage = error.message || 'Failed to submit report. Please try again.';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/']);
  }

  getErrorMessage(controlName: string): string {
    const control = this.reportForm.get(controlName);
    if (!control || !control.touched) return '';
    if (control.hasError('required')) return `${controlName} is required`;
    if (control.hasError('minlength')) return `Description must be at least ${control.errors?.['minlength'].requiredLength} characters`;
    return '';
  }
}
