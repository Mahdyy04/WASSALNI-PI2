import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RideService } from '../../../core/services/ride.service';
import { Ride } from '../../../core/models/ride.model';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Admin Rides component
 * Shows all rides history
 */
@Component({
  selector: 'app-admin-rides',
  standalone: true,
  imports: [CommonModule, DatePipe, CardComponent],
  template: `
    <div>
      <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-8">All Rides History</h1>
      
      <div class="space-y-4">
        @for (ride of rides; track ride.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div>
                <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                  {{ ride.departureCity }} → {{ ride.destinationCity }}
                </h3>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  Driver: {{ ride.driver?.name }} | 
                  Date: {{ ride.departureDate | date:'short' }} | 
                  Status: {{ ride.status }}
                </p>
              </div>
            </div>
          </app-card>
        } @empty {
          <app-card>
            <div class="text-center py-12">
              <p class="text-gray-600 dark:text-gray-400">No rides found</p>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class AdminRidesComponent implements OnInit {
  rides: Ride[] = [];

  constructor(private rideService: RideService) {}

  ngOnInit(): void {
    this.rideService.getAllRides().subscribe({
      next: (rides) => {
        this.rides = rides;
      },
      error: (error) => {
        console.error('Error loading rides:', error);
      }
    });
  }
}

