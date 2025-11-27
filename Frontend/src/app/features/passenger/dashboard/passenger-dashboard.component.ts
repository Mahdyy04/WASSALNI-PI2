import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { RideService } from '../../../core/services/ride.service';
import { Ride } from '../../../core/models/ride.model';
import { Gender } from '../../../core/models/user.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InputComponent } from '../../../shared/components/input/input.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ReviewListComponent } from '../../reviews/review-list/review-list.component';

/**
 * Passenger Dashboard component
 * Main dashboard for passengers with:
 * - Search ride form
 * - Filters (date, departure, destination, gender)
 * - Ride results with driver info and booking button
 */
@Component({
  selector: 'app-passenger-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    ButtonComponent,
    InputComponent,
    InputComponent,
    CardComponent,
    ReviewListComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">
          Find Your Ride, {{ authStore.currentUser()?.name }}!
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-2">
          Search for available rides to your destination
        </p>
      </div>

      <!-- Search Form -->
      <app-card class="mb-8">
        <form [formGroup]="searchForm" (ngSubmit)="searchRides()">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            <app-input
              id="departureCity"
              label="From"
              type="text"
              placeholder="Tunis"
              formControlName="departureCity"
            />

            <app-input
              id="destinationCity"
              label="To"
              type="text"
              placeholder="Sfax"
              formControlName="destinationCity"
            />

            <app-input
              id="date"
              label="Date"
              type="date"
              formControlName="date"
            />

            <div class="mb-4">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                Driver Gender
              </label>
              <select
                formControlName="gender"
                class="input-field"
              >
                <option value="">Any</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
              </select>
            </div>
          </div>

          <app-button
            type="submit"
            variant="primary"
            size="lg"
            [fullWidth]="true"
          >
            <svg class="w-5 h-5 mr-2 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            Search Rides
          </app-button>
        </form>
      </app-card>

      <!-- Ride Results -->
      @if (searchPerformed) {
        <div class="mb-6">
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-4">
            Available Rides ({{ rides.length }})
          </h2>

          @if (rides.length > 0) {
            <div class="grid grid-cols-1 gap-6">
              @for (ride of rides; track ride.id) {
                <app-card>
                  <div class="flex items-start justify-between">
                    <div class="flex-1">
                      <div class="flex items-center space-x-4 mb-4">
                        <div class="w-12 h-12 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold">
                          {{ getInitials(ride.driver?.name || 'Driver') }}
                        </div>
                        <div>
                          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                            {{ ride.driver?.name || 'Unknown Driver' }}
                            @if (ride.driver?.rating) {
                              <span class="text-sm text-yellow-500 ml-2">
                                ⭐ {{ ride.driver?.rating?.toFixed(1) }}
                              </span>
                            }
                          </h3>
                          <p class="text-sm text-gray-600 dark:text-gray-400">
                            {{ ride.driver?.totalRides || 0 }} rides
                          </p>
                        </div>
                      </div>

                      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
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
                          <p class="text-sm text-gray-500 dark:text-gray-400">Available Seats</p>
                          <p class="font-semibold text-gray-900 dark:text-gray-100">
                            {{ ride.availableSeats }} seat(s)
                          </p>
                        </div>
                      </div>

                      @if (ride.driver?.vehicle) {
                        <div class="mb-4 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                          <p class="text-sm text-gray-600 dark:text-gray-400">
                            <span class="font-medium">Vehicle:</span>
                            {{ ride.driver?.vehicle?.brand }} {{ ride.driver?.vehicle?.model }}
                            ({{ ride.driver?.vehicle?.color }})
                          </p>
                        </div>
                      }
                    </div>

                    <div class="ml-6 text-right">
                      <app-button
                        variant="primary"
                        size="lg"
                        (onClick)="bookRide(ride)"
                      >
                        Book Ride
                      </app-button>
                    </div>
                  </div>
                </app-card>
              }
            </div>
          } @else {
            <app-card>
              <div class="text-center py-12">
                <svg class="w-16 h-16 mx-auto text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p class="text-gray-600 dark:text-gray-400">No rides found for your search criteria</p>
                <p class="text-sm text-gray-500 dark:text-gray-500 mt-2">Try adjusting your filters</p>
              </div>
            </app-card>
          }
        </div>
      }
      <!-- Reviews -->
      <div class="mt-8">
        <app-review-list [userId]="authStore.currentUser()?.id"></app-review-list>
      </div>
    </div>
  `,
  styles: []
})
export class PassengerDashboardComponent implements OnInit {
  searchForm: FormGroup;
  rides: Ride[] = [];
  searchPerformed = false;

  constructor(
    public authStore: AuthStore,
    private fb: FormBuilder,
    private rideService: RideService,
    private router: Router
  ) {
    this.searchForm = this.fb.group({
      departureCity: [''],
      destinationCity: [''],
      date: [''],
      gender: ['']
    });
  }

  ngOnInit(): void {
    // Optionally load all rides on init
  }

  searchRides(): void {
    this.searchPerformed = true;
    const formValue = this.searchForm.value;
    const passenger = this.authStore.currentUser();

    this.rideService.searchRides({
      departureCity: formValue.departureCity || undefined,
      destinationCity: formValue.destinationCity || undefined,
      date: formValue.date ? new Date(formValue.date) : undefined,
      gender: formValue.gender || undefined,
      passengerGender: passenger?.gender
    }).subscribe({
      next: (rides) => {
        this.rides = rides;
      },
      error: (error) => {
        console.error('Error searching rides:', error);
      }
    });
  }

  bookRide(ride: Ride): void {
    // Navigate to booking page or open booking modal
    this.router.navigate(['/passenger/book-ride', ride.id]);
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }
}

