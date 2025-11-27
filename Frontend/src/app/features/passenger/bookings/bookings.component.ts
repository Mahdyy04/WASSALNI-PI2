import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { BookingService } from '../../../core/services/booking.service';
import { Booking, BookingStatus } from '../../../core/models/ride.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Passenger Bookings component
 * Shows all bookings made by the passenger
 * Displays booking status tracking
 */
@Component({
  selector: 'app-passenger-bookings',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterModule, ButtonComponent, CardComponent],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">My Bookings</h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Track your ride bookings and status
        </p>
      </div>

      <div class="space-y-4">
        @for (booking of bookings; track booking.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center justify-between mb-4">
                  <div>
                    <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100">
                      {{ booking.ride?.departureCity || 'N/A' }} → {{ booking.ride?.destinationCity || 'N/A' }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {{ booking.ride?.departureDate | date:'short' }} at {{ booking.ride?.departureTime || 'N/A' }}
                    </p>
                  </div>
                  <span [class]="getStatusClass(booking.status)">
                    {{ booking.status }}
                  </span>
                </div>

                <div class="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4">
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Driver</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ booking.ride?.driver?.name || 'Unknown' }}
                    </p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Seats</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.seatsRequested }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Booked On</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ booking.createdAt | date:'short' }}
                    </p>
                  </div>
                </div>

                @if (booking.status === BookingStatus.ACCEPTED) {
                  <div class="mt-4 p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                    <p class="text-sm text-green-800 dark:text-green-400">
                      ✅ Your booking has been accepted! Contact your driver for details.
                    </p>
                  </div>
                }
              </div>

              @if (booking.status === BookingStatus.PENDING) {
                <div class="ml-4">
                  <app-button
                    variant="danger"
                    size="sm"
                    (onClick)="cancelBooking(booking.id)"
                  >
                    Cancel
                  </app-button>
                </div>
              }
            </div>
          </app-card>
        } @empty {
          <app-card>
            <div class="text-center py-12">
              <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p class="text-gray-600 dark:text-gray-400 mb-4">No bookings yet</p>
              <app-button variant="primary" (onClick)="navigateToDashboard()">
                Search for Rides
              </app-button>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class PassengerBookingsComponent implements OnInit {
  bookings: Booking[] = [];

  constructor(
    private authStore: AuthStore,
    private bookingService: BookingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    const user = this.authStore.currentUser();
    if (user) {
      this.bookingService.getPassengerBookings(user.id).subscribe({
        next: (bookings) => {
          this.bookings = bookings;
        },
        error: (error) => {
          console.error('Error loading bookings:', error);
        }
      });
    }
  }

  cancelBooking(bookingId: string): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      const user = this.authStore.currentUser();
      const passengerId = user?.id;
      this.bookingService.cancelBooking(bookingId, passengerId).subscribe({
        next: () => {
          this.loadBookings();
        },
        error: (error) => {
          console.error('Error cancelling booking:', error);
        }
      });
    }
  }

  navigateToDashboard(): void {
    this.router.navigate(['/passenger/dashboard']);
  }

  getStatusClass(status: BookingStatus): string {
    const baseClasses = 'px-3 py-1 rounded-full text-sm font-medium';
    switch (status) {
      case BookingStatus.ACCEPTED:
        return `${baseClasses} bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400`;
      case BookingStatus.REJECTED:
        return `${baseClasses} bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400`;
      case BookingStatus.PENDING:
        return `${baseClasses} bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400`;
      case BookingStatus.CANCELLED:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
      default:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
    }
  }

  BookingStatus = BookingStatus;
}

