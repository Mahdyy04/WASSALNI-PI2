import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { Booking, BookingStatus } from '../models/ride.model';
import { Ride } from '../models/ride.model';
import { environment } from '../../../environments/environment';

/**
 * Backend Booking interface
 */
interface BackendBooking {
  id: string; // ← Changé de number à string
  rideId: string; // ← Changé de number à string
  passengerId: string; // ← Changé de number à string
  seatsBooked: number;
  status: string;
  createdAt: string;
  updatedAt?: string;
}

interface BackendBookingResponse {
  bookingId: string; // ← Changé de number à string
  rideId: string; // ← Changé de number à string
  passengerId: string; // ← Changé de number à string
  seatsBooked: number;
  status: string;
}

/**
 * Booking Service
 * 
 * Handles booking API calls only.
 * Does NOT manage state - use BookingsStore for state management.
 */
@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = `${environment.apiUrl}${environment.services.bookings}`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new booking
   */
 createBooking(rideId: string, passengerId: string, seatsRequested: number, ride: Ride): Observable<Booking> {
  const request = {
    rideId: rideId, // ← Supprimé parseInt()
    passengerId: passengerId, // ← Supprimé parseInt()
    seats: seatsRequested
  };

  return this.http.post<BackendBookingResponse>(`${this.apiUrl}/create`, request).pipe(
    map(response => this.mapBackendBookingToFrontend(response, ride)),
    catchError(error => {
      return throwError(() => new Error(error.error?.message || 'Failed to create booking'));
    })
  );
}

  /**
   * Get bookings by passenger ID
   */
  getPassengerBookings(passengerId: string): Observable<Booking[]> {
    return this.http.get<BackendBooking[]>(`${this.apiUrl}/passenger/${passengerId}`).pipe(
      map(bookings => bookings.map(b => this.mapBackendBookingToFrontend(b))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch bookings'));
      })
    );
  }

  /**
   * Get booking requests for a driver's rides
   */
  getDriverBookingRequests(driverId: string): Observable<Booking[]> {
    return this.http.get<BackendBooking[]>(`${this.apiUrl}/driver/${driverId}/pending`).pipe(
      map(bookings => bookings.map(b => this.mapBackendBookingToFrontend(b))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch booking requests'));
      })
    );
  }

  /**
   * Get all bookings
   */
  getAllBookings(): Observable<Booking[]> {
    // Backend doesn't have this endpoint, return empty for now
    return new Observable(observer => {
      observer.next([]);
      observer.complete();
    });
  }

  /**
   * Accept a booking
   */
  acceptBooking(bookingId: string, driverId?: string): Observable<Booking> {
    // Note: Backend requires driverId, get from auth store or parameter
    return this.http.post<string>(`${this.apiUrl}/${bookingId}/accept`, null, {
      params: new HttpParams().set('driverId', driverId || '1') // TODO: Get from auth
    }).pipe(
      map(() => {
        // Create a minimal booking object since backend doesn't return the updated booking
        return {
          id: bookingId,
          rideId: '',
          ride: {} as Ride,
          passengerId: '',
          passenger: {} as any,
          status: BookingStatus.ACCEPTED,
          seatsRequested: 0,
          createdAt: new Date()
        } as Booking;
      }),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to accept booking'));
      })
    ) as any;
  }

  /**
   * Reject a booking
   */
  rejectBooking(bookingId: string, driverId?: string): Observable<Booking> {
    return this.http.post<string>(`${this.apiUrl}/${bookingId}/reject`, null, {
      params: new HttpParams().set('driverId', driverId || '1') // TODO: Get from auth
    }).pipe(
      map(() => {
        // Create a minimal booking object since backend doesn't return the updated booking
        return {
          id: bookingId,
          rideId: '',
          ride: {} as Ride,
          passengerId: '',
          passenger: {} as any,
          status: BookingStatus.REJECTED,
          seatsRequested: 0,
          createdAt: new Date()
        } as Booking;
      }),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to reject booking'));
      })
    );
  }

  /**
   * Cancel a booking
   */
  cancelBooking(bookingId: string, passengerId?: string): Observable<void> {
    return this.http.delete<string>(`${this.apiUrl}/${bookingId}`, {
      params: new HttpParams().set('passengerId', passengerId || '1') // TODO: Get from auth
    }).pipe(
      map(() => void 0),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to cancel booking'));
      })
    );
  }

  /**
   * Map backend booking to frontend booking
   */
  private mapBackendBookingToFrontend(backendBooking: BackendBooking | BackendBookingResponse, ride?: Ride): Booking {
  const isResponse = 'bookingId' in backendBooking;
  return {
    id: isResponse ? backendBooking.bookingId : backendBooking.id, // ← Supprimé .toString()
    rideId: backendBooking.rideId, // ← Supprimé .toString()
    ride: ride || {} as Ride,
    passengerId: backendBooking.passengerId, // ← Supprimé .toString()
    passenger: {} as any,
    status: backendBooking.status as BookingStatus,
    seatsRequested: backendBooking.seatsBooked,
    createdAt: isResponse ? new Date() : new Date(backendBooking.createdAt)
  };
}
}

