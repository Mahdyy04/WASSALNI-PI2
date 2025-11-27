import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { RideService } from '../../../core/services/ride.service';
import { BookingService } from '../../../core/services/booking.service';
import { ReviewService } from '../../../core/services/review.service';
import { Ride } from '../../../core/models/ride.model';
import { Booking, BookingStatus } from '../../../core/models/ride.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ReviewListComponent } from '../../reviews/review-list/review-list.component';

/**
 * Driver Dashboard component
 * Main dashboard for drivers showing:
 * - Quick stats
 * - Publish ride button
 * - Recent rides
 * - Booking requests
 */
@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterModule, ButtonComponent, CardComponent, ReviewListComponent],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">
          Welcome back, {{ authStore.currentUser()?.name }}!
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Manage your rides and bookings from here
        </p>
      </div>

      <!-- Quick Actions -->
      <div class="mb-8">
        <app-button
          variant="primary"
          size="lg"
          (onClick)="navigateToPublishRide()"
        >
          <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Publish New Ride
        </app-button>
      </div>

      <!-- Stats Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Total Rides</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ totalRides }}</p>
            </div>
            <div class="w-12 h-12 bg-primary-100 dark:bg-primary-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-primary-600 dark:text-primary-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </app-card>

        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Pending Requests</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ pendingBookings }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 dark:bg-yellow-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-yellow-600 dark:text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
          </div>
        </app-card>

        <app-card>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Rating</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ averageRating || 'N/A' }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
              <svg class="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z" />
              </svg>
            </div>
          </div>
        </app-card>
      </div>

      <!-- Recent Rides -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <app-card title="My Recent Rides">
          <div class="space-y-4">
            @for (ride of recentRides; track ride.id) {
              <div class="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div>
                  <p class="font-semibold text-gray-900 dark:text-gray-100">{{ ride.departureCity }} → {{ ride.destinationCity }}</p>
                  <p class="text-sm text-gray-600 dark:text-gray-400">{{ ride.departureDate | date:'short' }} at {{ ride.departureTime }}</p>
                </div>
                <span class="px-3 py-1 bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400 rounded-full text-sm font-medium">
                  {{ ride.availableSeats }} seats
                </span>
              </div>
            } @empty {
              <p class="text-gray-600 dark:text-gray-400 text-center py-4">No recent rides</p>
            }
            <div class="text-center">
              <a routerLink="/driver/rides" class="text-primary-600 hover:text-primary-700 font-medium">
                View all rides →
              </a>
            </div>
          </div>
        </app-card>

        <app-card title="Booking Requests">
          <div class="space-y-4">
            @for (booking of recentBookings; track booking.id) {
              <div class="p-4 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                <div class="flex items-center justify-between mb-2">
                  <p class="font-semibold text-gray-900 dark:text-gray-100">{{ booking.passenger?.name || 'Unknown' }}</p>
                  <span class="px-2 py-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400 rounded text-xs font-medium">
                    Pending
                  </span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">{{ booking.seatsRequested }} seat(s) requested</p>
                <div class="flex space-x-2">
                  <app-button variant="primary" size="sm" (onClick)="acceptBooking(booking.id)">
                    Accept
                  </app-button>
                  <app-button variant="danger" size="sm" (onClick)="rejectBooking(booking.id)">
                    Reject
                  </app-button>
                </div>
              </div>
            } @empty {
              <p class="text-gray-600 dark:text-gray-400 text-center py-4">No pending requests</p>
            }
            <div class="text-center">
              <a routerLink="/driver/bookings" class="text-primary-600 hover:text-primary-700 font-medium">
                View all requests →
              </a>
            </div>
          </div>
        </app-card>
      </div>
      <!-- Reviews -->
      <div class="mt-8">
        <app-review-list [userId]="authStore.currentUser()?.id"></app-review-list>
      </div>
    </div>
  `,
  styles: []
})
export class DriverDashboardComponent implements OnInit {
  totalRides = 0;
  pendingBookings = 0;
  averageRating: number | null = null;
  recentRides: Ride[] = [];
  recentBookings: Booking[] = [];

  constructor(
    public authStore: AuthStore,
    private rideService: RideService,
    private bookingService: BookingService,
    private reviewService: ReviewService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    const user = this.authStore.currentUser();
    if (!user) return;

    // Load driver rides
    this.rideService.getDriverRides(user.id).subscribe({
      next: (rides) => {
        this.totalRides = rides.length;
        this.recentRides = rides.slice(0, 3); // Show 3 most recent
      },
      error: (error) => {
        console.error('Error loading rides:', error);
      }
    });

    // Load pending bookings
    this.bookingService.getDriverBookingRequests(user.id).subscribe({
      next: (bookings) => {
        this.pendingBookings = bookings.filter(b => b.status === BookingStatus.PENDING).length;
        this.recentBookings = bookings.filter(b => b.status === BookingStatus.PENDING).slice(0, 3);
      },
      error: (error) => {
        console.error('Error loading bookings:', error);
      }
    });

    // Load average rating
    this.reviewService.getAverageRating(user.id).subscribe({
      next: (rating) => {
        this.averageRating = rating;
      },
      error: (error) => {
        console.error('Error loading rating:', error);
      }
    });
  }

  navigateToPublishRide(): void {
    this.router.navigate(['/driver/publish-ride']);
  }

  acceptBooking(bookingId: string): void {
    const user = this.authStore.currentUser();
    const driverId = user?.id;
    this.bookingService.acceptBooking(bookingId, driverId).subscribe({
      next: () => {
        this.loadDashboardData(); // Refresh data
      },
      error: (error) => {
        console.error('Error accepting booking:', error);
        alert('Failed to accept booking. Please try again.');
      }
    });
  }

  rejectBooking(bookingId: string): void {
    const user = this.authStore.currentUser();
    const driverId = user?.id;
    this.bookingService.rejectBooking(bookingId, driverId).subscribe({
      next: () => {
        this.loadDashboardData(); // Refresh data
      },
      error: (error) => {
        console.error('Error rejecting booking:', error);
        alert('Failed to reject booking. Please try again.');
      }
    });
  }
}

