import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthStore } from '../../../core/state/auth.store';
import { BookingService } from '../../../core/services/booking.service';
import { Booking, BookingStatus } from '../../../core/models/ride.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Driver Bookings component
 * Shows all booking requests for driver's rides
 * Allows accepting/rejecting bookings
 */
@Component({
  selector: 'app-driver-bookings',
  standalone: true,
  imports: [CommonModule, DatePipe, ButtonComponent, CardComponent],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Booking Requests</h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Manage booking requests for your rides
        </p>
      </div>

      <div class="space-y-4">
        @for (booking of bookings; track booking.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold">
                    {{ getInitials(booking.passenger?.name || 'Unknown') }}
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                      {{ booking.passenger?.name || 'Unknown Passenger' }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400">
                      {{ booking.ride?.departureCity || 'N/A' }} → {{ booking.ride?.destinationCity || 'N/A' }}
                    </p>
                  </div>
                </div>

                <div class="grid grid-cols-2 md:grid-cols-3 gap-4 mb-4">
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Seats</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">{{ booking.seatsRequested }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Date</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ booking.ride?.departureDate | date:'short' }}
                    </p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Status</p>
                    <span [class]="getStatusClass(booking.status)">
                      {{ booking.status }}
                    </span>
                  </div>
                </div>
              </div>

              @if (booking.status === BookingStatus.PENDING) {
                <div class="flex flex-col space-y-2 ml-4">
                  <app-button
                    variant="primary"
                    size="sm"
                    (onClick)="acceptBooking(booking.id)"
                    [loading]="loadingBookings.has(booking.id)"
                  >
                    Accept
                  </app-button>
                  <app-button
                    variant="danger"
                    size="sm"
                    (onClick)="rejectBooking(booking.id)"
                    [loading]="loadingBookings.has(booking.id)"
                  >
                    Reject
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
              <p class="text-gray-600 dark:text-gray-400">No booking requests yet</p>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class DriverBookingsComponent implements OnInit {
  bookings: Booking[] = [];
  loadingBookings = new Set<string>();

  constructor(
    private authStore: AuthStore,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
    const user = this.authStore.currentUser();
    if (user) {
      this.bookingService.getDriverBookingRequests(user.id).subscribe({
        next: (bookings) => {
          this.bookings = bookings;
        },
        error: (error) => {
          console.error('Error loading bookings:', error);
        }
      });
    }
  }

  acceptBooking(bookingId: string): void {
    this.loadingBookings.add(bookingId);
    const user = this.authStore.currentUser();
    const driverId = user?.id;
    this.bookingService.acceptBooking(bookingId, driverId).subscribe({
      next: () => {
        this.loadingBookings.delete(bookingId);
        this.loadBookings(); // Reload bookings
      },
      error: (error) => {
        this.loadingBookings.delete(bookingId);
        console.error('Error accepting booking:', error);
      }
    });
  }

  rejectBooking(bookingId: string): void {
    this.loadingBookings.add(bookingId);
    const user = this.authStore.currentUser();
    const driverId = user?.id;
    this.bookingService.rejectBooking(bookingId, driverId).subscribe({
      next: () => {
        this.loadingBookings.delete(bookingId);
        this.loadBookings(); // Reload bookings
      },
      error: (error) => {
        this.loadingBookings.delete(bookingId);
        console.error('Error rejecting booking:', error);
      }
    });
  }

  getStatusClass(status: BookingStatus): string {
    const baseClasses = 'px-2 py-1 rounded-full text-xs font-medium';
    switch (status) {
      case BookingStatus.ACCEPTED:
        return `${baseClasses} bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400`;
      case BookingStatus.REJECTED:
        return `${baseClasses} bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400`;
      case BookingStatus.PENDING:
        return `${baseClasses} bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400`;
      default:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
    }
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  BookingStatus = BookingStatus;
}

