import { Injectable, signal, computed } from '@angular/core';
import { Ride, RideStatus } from '../models/ride.model';
import { Gender } from '../models/user.model';

/**
 * Rides Store
 * 
 * Manages ride-related state including:
 * - List of available rides
 * - Currently selected ride
 * - Ride search filters
 * - Filtered ride results
 * 
 * Uses Angular Signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class RidesStore {
  // ============================================================================
  // Private State Signals
  // ============================================================================

  /**
   * Private signal storing all rides
   * This is the source of truth for rides data
   */
  private readonly ridesSignal = signal<Ride[]>([]);

  /**
   * Private signal storing the currently selected ride
   * Used for ride detail views
   */
  private readonly selectedRideSignal = signal<Ride | null>(null);

  /**
   * Private signal storing search filters
   * Used for filtering available rides
   */
  private readonly filtersSignal = signal<RideFilters>({
    departureCity: '',
    destinationCity: '',
    date: null,
    gender: null,
    passengerGender: null
  });

  // ============================================================================
  // Public Readonly Computed Signals
  // ============================================================================

  /**
   * All rides (readonly)
   */
  public readonly rides = computed(() => this.ridesSignal());

  /**
   * Selected ride (readonly)
   */
  public readonly selectedRide = computed(() => this.selectedRideSignal());

  /**
   * Current search filters (readonly)
   */
  public readonly filters = computed(() => this.filtersSignal());

  /**
   * Filtered rides based on current filters (readonly)
   * Automatically computed when rides or filters change
   */
  public readonly filteredRides = computed(() => {
    const rides = this.ridesSignal();
    const filters = this.filtersSignal();

    return rides.filter(ride => {
      // Filter by departure city
      if (filters.departureCity && 
          !ride.departureCity.toLowerCase().includes(filters.departureCity.toLowerCase())) {
        return false;
      }

      // Filter by destination city
      if (filters.destinationCity && 
          !ride.destinationCity.toLowerCase().includes(filters.destinationCity.toLowerCase())) {
        return false;
      }

      // Filter by date
      if (filters.date) {
        const filterDate = new Date(filters.date);
        const rideDate = new Date(ride.departureDate);
        if (filterDate.toDateString() !== rideDate.toDateString()) {
          return false;
        }
      }

      // Filter by driver gender (if passenger gender is female)
      if (filters.passengerGender === Gender.FEMALE && filters.gender) {
        if (ride.driver?.gender !== filters.gender) {
          return false;
        }
      }

      // Only show available scheduled rides
      return ride.availableSeats > 0 && ride.status === RideStatus.SCHEDULED;
    });
  });

  /**
   * Available rides count (readonly)
   * Computed from filtered rides
   */
  public readonly availableRidesCount = computed(() => 
    this.filteredRides().length
  );

  /**
   * User's rides (readonly)
   * Filtered by driver ID - requires user ID to be passed
   * Note: This is a helper that requires filtering by driverId in components
   */
  public readonly ridesByDriverId = (driverId: string) => 
    computed(() => 
      this.ridesSignal().filter(ride => ride.driverId === driverId)
    );

  // ============================================================================
  // Constructor
  // ============================================================================

  constructor() {
    // Store initialization
  }

  // ============================================================================
  // State Update Methods (Setters)
  // ============================================================================

  /**
   * Set all rides
   * Called when rides are loaded from API
   * 
   * @param rides Array of rides to store
   */
  setRides(rides: Ride[]): void {
    this.ridesSignal.set(rides);
  }

  /**
   * Add a new ride
   * Called when a new ride is created
   * 
   * @param ride The new ride to add
   */
  addRide(ride: Ride): void {
    this.ridesSignal.update(rides => [...rides, ride]);
  }

  /**
   * Update an existing ride
   * Called when ride details are updated
   * 
   * @param rideId The ID of the ride to update
   * @param updates Partial ride object with fields to update
   */
  updateRide(rideId: string, updates: Partial<Ride>): void {
    this.ridesSignal.update(rides =>
      rides.map(ride =>
        ride.id === rideId ? { ...ride, ...updates } : ride
      )
    );

    // Update selected ride if it's the one being updated
    const selectedRide = this.selectedRideSignal();
    if (selectedRide?.id === rideId) {
      const updatedRide = this.ridesSignal().find(r => r.id === rideId);
      if (updatedRide) {
        this.selectedRideSignal.set(updatedRide);
      }
    }
  }

  /**
   * Remove a ride
   * Called when a ride is deleted or cancelled
   * 
   * @param rideId The ID of the ride to remove
   */
  removeRide(rideId: string): void {
    this.ridesSignal.update(rides => rides.filter(ride => ride.id !== rideId));
    
    // Clear selected ride if it's the one being removed
    if (this.selectedRideSignal()?.id === rideId) {
      this.selectedRideSignal.set(null);
    }
  }

  /**
   * Set selected ride
   * Called when user views ride details
   * 
   * @param ride The ride to select, or null to clear selection
   */
  setSelectedRide(ride: Ride | null): void {
    this.selectedRideSignal.set(ride);
  }

  /**
   * Set search filters
   * Called when user updates search criteria
   * 
   * @param filters Partial filters object with fields to update
   */
  setFilters(filters: Partial<RideFilters>): void {
    const currentFilters = this.filtersSignal();
    this.filtersSignal.set({ ...currentFilters, ...filters });
  }

  /**
   * Clear search filters
   * Resets all filters to default values
   */
  clearFilters(): void {
    this.filtersSignal.set({
      departureCity: '',
      destinationCity: '',
      date: null,
      gender: null,
      passengerGender: null
    });
  }

  /**
   * Clear all rides
   * Called on logout or when rides need to be cleared
   */
  clearRides(): void {
    this.ridesSignal.set([]);
    this.selectedRideSignal.set(null);
    this.clearFilters();
  }
}

// ============================================================================
// Type Definitions
// ============================================================================

/**
 * Ride filters interface
 * Defines all possible filters for ride searches
 */
export interface RideFilters {
  departureCity: string;
  destinationCity: string;
  date: Date | null;
  gender: Gender | null;
  passengerGender: Gender | null;
}

