import { Injectable, signal, computed } from '@angular/core';
import { Booking, BookingStatus } from '../models/ride.model';

/**
 * Bookings Store
 * 
 * Manages booking-related state including:
 * - List of all bookings
 * - Passenger bookings
 * - Driver booking requests (pending bookings for driver's rides)
 * - Booking statistics
 * 
 * Uses Angular Signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class BookingsStore {
  // ============================================================================
  // Private State Signals
  // ============================================================================

  /**
   * Private signal storing all bookings
   * This is the source of truth for bookings data
   */
  private readonly bookingsSignal = signal<Booking[]>([]);

  /**
   * Private signal storing selected booking
   * Used for booking detail views
   */
  private readonly selectedBookingSignal = signal<Booking | null>(null);

  // ============================================================================
  // Public Readonly Computed Signals
  // ============================================================================

  /**
   * All bookings (readonly)
   */
  public readonly bookings = computed(() => this.bookingsSignal());

  /**
   * Selected booking (readonly)
   */
  public readonly selectedBooking = computed(() => this.selectedBookingSignal());

  /**
   * Bookings by passenger ID (readonly)
   * Helper computed that requires passenger ID
   * Usage: bookingsStore.bookingsByPassengerId(passengerId)()
   */
  public readonly bookingsByPassengerId = (passengerId: string) =>
    computed(() =>
      this.bookingsSignal().filter(booking => booking.passengerId === passengerId)
    );

  /**
   * Bookings by driver ID (readonly)
   * Helper computed that requires driver ID
   * Usage: bookingsStore.bookingsByDriverId(driverId)()
   */
  public readonly bookingsByDriverId = (driverId: string) =>
    computed(() =>
      this.bookingsSignal().filter(booking => booking.ride?.driverId === driverId)
    );

  /**
   * Pending booking requests for a driver (readonly)
   * Helper computed that requires driver ID
   * Usage: bookingsStore.pendingRequestsForDriver(driverId)()
   */
  public readonly pendingRequestsForDriver = (driverId: string) =>
    computed(() =>
      this.bookingsSignal().filter(
        booking =>
          booking.ride?.driverId === driverId &&
          booking.status === BookingStatus.PENDING
      )
    );

  /**
   * Bookings by status (readonly)
   * Helper computed that requires status
   * Usage: bookingsStore.bookingsByStatus(BookingStatus.ACCEPTED)()
   */
  public readonly bookingsByStatus = (status: BookingStatus) =>
    computed(() =>
      this.bookingsSignal().filter(booking => booking.status === status)
    );

  /**
   * Total bookings count (readonly)
   */
  public readonly totalBookingsCount = computed(() => 
    this.bookingsSignal().length
  );

  /**
   * Pending bookings count (readonly)
   */
  public readonly pendingBookingsCount = computed(() =>
    this.bookingsSignal().filter(b => b.status === BookingStatus.PENDING).length
  );

  /**
   * Accepted bookings count (readonly)
   */
  public readonly acceptedBookingsCount = computed(() =>
    this.bookingsSignal().filter(b => b.status === BookingStatus.ACCEPTED).length
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
   * Set all bookings
   * Called when bookings are loaded from API
   * 
   * @param bookings Array of bookings to store
   */
  setBookings(bookings: Booking[]): void {
    this.bookingsSignal.set(bookings);
  }

  /**
   * Add a new booking
   * Called when a new booking is created
   * 
   * @param booking The new booking to add
   */
  addBooking(booking: Booking): void {
    this.bookingsSignal.update(bookings => [...bookings, booking]);
  }

  /**
   * Update an existing booking
   * Called when booking status or details are updated
   * 
   * @param bookingId The ID of the booking to update
   * @param updates Partial booking object with fields to update
   */
  updateBooking(bookingId: string, updates: Partial<Booking>): void {
    this.bookingsSignal.update(bookings =>
      bookings.map(booking =>
        booking.id === bookingId ? { ...booking, ...updates } : booking
      )
    );

    // Update selected booking if it's the one being updated
    const selectedBooking = this.selectedBookingSignal();
    if (selectedBooking?.id === bookingId) {
      const updatedBooking = this.bookingsSignal().find(b => b.id === bookingId);
      if (updatedBooking) {
        this.selectedBookingSignal.set(updatedBooking);
      }
    }
  }

  /**
   * Update booking status
   * Convenience method for updating booking status
   * 
   * @param bookingId The ID of the booking to update
   * @param status The new status to set
   */
  updateBookingStatus(bookingId: string, status: BookingStatus): void {
    this.updateBooking(bookingId, { status });
  }

  /**
   * Remove a booking
   * Called when a booking is cancelled or deleted
   * 
   * @param bookingId The ID of the booking to remove
   */
  removeBooking(bookingId: string): void {
    this.bookingsSignal.update(bookings => 
      bookings.filter(booking => booking.id !== bookingId)
    );

    // Clear selected booking if it's the one being removed
    if (this.selectedBookingSignal()?.id === bookingId) {
      this.selectedBookingSignal.set(null);
    }
  }

  /**
   * Set selected booking
   * Called when user views booking details
   * 
   * @param booking The booking to select, or null to clear selection
   */
  setSelectedBooking(booking: Booking | null): void {
    this.selectedBookingSignal.set(booking);
  }

  /**
   * Accept a booking
   * Convenience method that updates booking status to ACCEPTED
   * 
   * @param bookingId The ID of the booking to accept
   */
  acceptBooking(bookingId: string): void {
    this.updateBookingStatus(bookingId, BookingStatus.ACCEPTED);
  }

  /**
   * Reject a booking
   * Convenience method that updates booking status to REJECTED
   * 
   * @param bookingId The ID of the booking to reject
   */
  rejectBooking(bookingId: string): void {
    this.updateBookingStatus(bookingId, BookingStatus.REJECTED);
  }

  /**
   * Cancel a booking
   * Convenience method that updates booking status to CANCELLED
   * 
   * @param bookingId The ID of the booking to cancel
   */
  cancelBooking(bookingId: string): void {
    this.updateBookingStatus(bookingId, BookingStatus.CANCELLED);
  }

  /**
   * Clear all bookings
   * Called on logout or when bookings need to be cleared
   */
  clearBookings(): void {
    this.bookingsSignal.set([]);
    this.selectedBookingSignal.set(null);
  }
}

