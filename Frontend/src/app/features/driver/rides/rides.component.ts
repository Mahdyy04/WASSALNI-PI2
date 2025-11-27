import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { RideService } from '../../../core/services/ride.service';
import { Ride, RideStatus } from '../../../core/models/ride.model';
import { formatDateTime } from '../../../core/utils/date.util';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Driver Rides component
 * Lists all rides published by the driver
 */
@Component({
  selector: 'app-driver-rides',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterModule, ButtonComponent, CardComponent],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="flex justify-between items-center mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">My Rides</h1>
          <p class="text-gray-600 dark:text-gray-400 mt-2">
            All rides you've published
          </p>
        </div>
        <app-button
          variant="primary"
          (onClick)="navigateToPublishRide()"
        >
          <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
          </svg>
          Publish New Ride
        </app-button>
      </div>

      <div class="grid grid-cols-1 gap-6">
        @for (ride of rides; track ride.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-4 mb-4">
                  <div>
                    <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100">
                      {{ ride.departureCity }} → {{ ride.destinationCity }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400 mt-1">
                      {{ formatDateTime(ride.departureDate, ride.departureTime) }}
                    </p>
                  </div>
                  <span [class]="getStatusClass(ride.status)">
                    {{ ride.status }}
                  </span>
                </div>

                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Available Seats</p>
                    <p class="font-semibold text-gray-900 dark:text-gray-100">{{ ride.availableSeats }} / {{ ride.totalSeats }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Passengers</p>
                    <p class="font-semibold text-gray-900 dark:text-gray-100">{{ ride.passengers?.length || 0 }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Created</p>
                    <p class="font-semibold text-gray-900 dark:text-gray-100">{{ ride.createdAt | date:'short' }}</p>
                  </div>
                </div>
              </div>
            </div>
          </app-card>
        } @empty {
          <app-card>
            <div class="text-center py-12">
              <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <p class="text-gray-600 dark:text-gray-400 mb-4">No rides published yet</p>
              <app-button variant="primary" (onClick)="navigateToPublishRide()">
                Publish Your First Ride
              </app-button>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class DriverRidesComponent implements OnInit {
  rides: Ride[] = [];
  formatDateTime = formatDateTime;

  constructor(
    private authStore: AuthStore,
    private rideService: RideService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadRides();
  }

  loadRides(): void {
    const user = this.authStore.currentUser();
    if (user) {
      this.rideService.getDriverRides(user.id).subscribe({
        next: (rides) => {
          this.rides = rides;
        },
        error: (error) => {
          console.error('Error loading rides:', error);
        }
      });
    }
  }

  navigateToPublishRide(): void {
    this.router.navigate(['/driver/publish-ride']);
  }

  getStatusClass(status: RideStatus): string {
    const baseClasses = 'px-3 py-1 rounded-full text-sm font-medium';
    switch (status) {
      case RideStatus.SCHEDULED:
        return `${baseClasses} bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400`;
      case RideStatus.IN_PROGRESS:
        return `${baseClasses} bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400`;
      case RideStatus.COMPLETED:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
      case RideStatus.CANCELLED:
        return `${baseClasses} bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400`;
      default:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
    }
  }

  RideStatus = RideStatus;
}

