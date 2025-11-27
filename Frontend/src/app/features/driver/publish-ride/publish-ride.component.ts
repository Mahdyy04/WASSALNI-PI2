import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { RidesStore } from '../../../core/state/rides.store';
import { RideService } from '../../../core/services/ride.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InputComponent } from '../../../shared/components/input/input.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Publish Ride component
 * Form for drivers to create and publish a new ride
 */
@Component({
  selector: 'app-publish-ride',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonComponent,
    InputComponent,
    CardComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-8 max-w-2xl">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Publish a Ride</h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Share your journey and help others travel affordably
        </p>
      </div>

      <app-card>
        <form [formGroup]="publishRideForm" (ngSubmit)="onSubmit()">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <app-input
              id="departureCity"
              label="Departure City"
              type="text"
              placeholder="Tunis"
              formControlName="departureCity"
              [required]="true"
              [error]="getErrorMessage('departureCity')"
            />

            <app-input
              id="destinationCity"
              label="Destination City"
              type="text"
              placeholder="Sfax"
              formControlName="destinationCity"
              [required]="true"
              [error]="getErrorMessage('destinationCity')"
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <app-input
              id="departureDate"
              label="Departure Date"
              type="date"
              formControlName="departureDate"
              [required]="true"
              [error]="getErrorMessage('departureDate')"
            />

            <app-input
              id="departureTime"
              label="Departure Time"
              type="time"
              formControlName="departureTime"
              [required]="true"
              [error]="getErrorMessage('departureTime')"
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <app-input
              id="availableSeats"
              label="Available Seats"
              type="number"
              placeholder="3"
              formControlName="availableSeats"
              [required]="true"
              [error]="getErrorMessage('availableSeats')"
            />
          </div>

          @if (errorMessage) {
            <div class="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
            </div>
          }

          <div class="flex space-x-4 mt-6">
            <app-button
              type="submit"
              variant="primary"
              size="lg"
              [loading]="loading"
            >
              Publish Ride
            </app-button>
            <app-button
              type="button"
              variant="secondary"
              size="lg"
              (onClick)="cancel()"
            >
              Cancel
            </app-button>
          </div>
        </form>
      </app-card>
    </div>
  `,
  styles: []
})
export class PublishRideComponent {
  publishRideForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authStore: AuthStore,
    private ridesStore: RidesStore,
    private rideService: RideService,
    private router: Router
  ) {
    // Set minimum date to today
    const today = new Date().toISOString().split('T')[0];

    this.publishRideForm = this.fb.group({
      departureCity: ['', [Validators.required]],
      destinationCity: ['', [Validators.required]],
      departureDate: ['', [Validators.required]],
      departureTime: ['', [Validators.required]],
      availableSeats: [null, [Validators.required, Validators.min(1)]]
    });
  }

  onSubmit(): void {
    if (this.publishRideForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const formValue = this.publishRideForm.value;
      const user = this.authStore.currentUser();

      if (!user) {
        this.errorMessage = 'You must be logged in to publish a ride';
        this.loading = false;
        return;
      }

      const rideData = {
        driverId: user.id,
        driver: user,
        departureCity: formValue.departureCity,
        destinationCity: formValue.destinationCity,
        departureDate: new Date(formValue.departureDate),
        departureTime: formValue.departureTime,
        availableSeats: formValue.availableSeats,
        totalSeats: formValue.availableSeats
      };

      // Call service for API call, then update store with state
      this.rideService.createRide(rideData).subscribe({
        next: (ride) => {
          // Update rides store with new ride
          this.ridesStore.addRide(ride);
          
          this.loading = false;
          this.router.navigate(['/driver/dashboard']);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.message || 'Failed to publish ride. Please try again.';
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/driver/dashboard']);
  }

  getErrorMessage(controlName: string): string {
    const control = this.publishRideForm.get(controlName);
    if (control?.hasError('required') && control.touched) {
      return `${controlName.charAt(0).toUpperCase() + controlName.slice(1).replace(/([A-Z])/g, ' $1')} is required`;
    }
    if (control?.hasError('min') && control.touched) {
      return `Must be at least ${control.errors?.['min'].min}`;
    }
    return '';
  }
}

