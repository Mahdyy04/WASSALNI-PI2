import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthStore } from '../../../core/state/auth.store';
import { RidesStore } from '../../../core/state/rides.store';
import { BookingsStore } from '../../../core/state/bookings.store';
import { RideService } from '../../../core/services/ride.service';
import { BookingService } from '../../../core/services/booking.service';
import { Ride } from '../../../core/models/ride.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InputComponent } from '../../../shared/components/input/input.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Book Ride component
 * Allows passengers to book a specific ride
 * Shows ride details and booking form
 */
@Component({
  selector: 'app-book-ride',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    ButtonComponent,
    InputComponent,
    CardComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-8 max-w-3xl">
      @if (ride) {
        <div class="mb-8">
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Book Ride</h1>
        </div>

        <!-- Ride Details -->
        <app-card class="mb-6">
          <h2 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">Ride Details</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Route</p>
              <p class="font-semibold text-gray-900 dark:text-gray-100">
                {{ ride.departureCity }} → {{ ride.destinationCity }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Departure</p>
              <p class="font-semibold text-gray-900 dark:text-gray-100">
                {{ ride.departureDate | date:'short' }} at {{ ride.departureTime }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-500 dark:text-gray-400">Driver</p>
              <p class="font-semibold text-gray-900 dark:text-gray-100">
                {{ ride.driver?.name || 'Unknown' }}
                            @if (ride.driver?.rating) {
                              <span class="text-yellow-500 ml-2">⭐ {{ ride.driver?.rating?.toFixed(1) }}</span>
                            }
              </p>
            </div>
          </div>
        </app-card>

        <!-- Booking Form -->
        <app-card>
          <h2 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-4">Booking Information</h2>
          <form [formGroup]="bookingForm" (ngSubmit)="onSubmit()">
            <app-input
              id="seats"
              label="Number of Seats"
              type="number"
              placeholder="1"
              formControlName="seats"
              [required]="true"
              [error]="getErrorMessage('seats')"
            />

            @if (errorMessage) {
              <div class="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
              </div>
            }

            <div class="flex space-x-4">
              <app-button
                type="submit"
                variant="primary"
                size="lg"
                [loading]="loading"
              >
                Confirm Booking
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
      } @else {
        <app-card>
          <div class="text-center py-12">
            <p class="text-gray-600 dark:text-gray-400">Loading ride details...</p>
          </div>
        </app-card>
      }
    </div>
  `,
  styles: []
})
export class BookRideComponent implements OnInit {
  ride: Ride | null = null;
  bookingForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private authStore: AuthStore,
    private ridesStore: RidesStore,
    private bookingsStore: BookingsStore,
    private rideService: RideService,
    private bookingService: BookingService
  ) {
    this.bookingForm = this.fb.group({
      seats: [1, [Validators.required, Validators.min(1)]]
    });
  }

  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('id');
    if (rideId) {
      this.loadRide(rideId);
    }
  }

  loadRide(rideId: string): void {
    // First check if ride is already in store
    const storeRide = this.ridesStore.rides().find(r => r.id === rideId);
    if (storeRide) {
      this.ride = storeRide;
      this.ridesStore.setSelectedRide(storeRide);
      this.setupFormValidation();
      return;
    }

    // If not in store, fetch from service and update store
    this.rideService.getRideById(rideId).subscribe({
      next: (ride) => {
        if (ride) {
          this.ride = ride;
          this.ridesStore.addRide(ride);
          this.ridesStore.setSelectedRide(ride);
          this.setupFormValidation();
        } else {
          this.router.navigate(['/passenger/dashboard']);
        }
      },
      error: (error) => {
        console.error('Error loading ride:', error);
        this.router.navigate(['/passenger/dashboard']);
      }
    });
  }

  private setupFormValidation(): void {
    if (this.ride) {
      // Set max seats validation
      this.bookingForm.get('seats')?.setValidators([
        Validators.required,
        Validators.min(1),
        Validators.max(this.ride.availableSeats)
      ]);
    }
  }

  onSubmit(): void {
    if (this.bookingForm.valid && this.ride) {
      this.loading = true;
      this.errorMessage = '';

      const seats = this.bookingForm.get('seats')?.value;
      const user = this.authStore.currentUser();

      if (!user) {
        this.errorMessage = 'You must be logged in to book a ride';
        this.loading = false;
        return;
      }

      if (seats > this.ride.availableSeats) {
        this.errorMessage = 'Not enough available seats';
        this.loading = false;
        return;
      }

      // Call service for API call, then update store with state
      this.bookingService.createBooking(this.ride.id, user.id, seats, this.ride).subscribe({
        next: (booking) => {
          // Update bookings store with new booking
          this.bookingsStore.addBooking(booking);
          
          // Update ride availability in rides store
          this.ridesStore.updateRide(this.ride!.id, {
            availableSeats: this.ride!.availableSeats - seats
          });
          
          this.loading = false;
          this.router.navigate(['/passenger/bookings']);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.message || 'Failed to book ride. Please try again.';
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/passenger/dashboard']);
  }

  getErrorMessage(controlName: string): string {
    const control = this.bookingForm.get(controlName);
    if (control?.hasError('required') && control.touched) {
      return 'Number of seats is required';
    }
    if (control?.hasError('min') && control.touched) {
      return 'Must book at least 1 seat';
    }
    if (control?.hasError('max') && control.touched) {
      return `Only ${this.ride?.availableSeats || 0} seats available`;
    }
    return '';
  }
}

