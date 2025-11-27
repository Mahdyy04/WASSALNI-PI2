import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, catchError, throwError, of } from 'rxjs';
import { Ride, RideStatus } from '../models/ride.model';
import { User, UserRole, Gender } from '../models/user.model';
import { environment } from '../../../environments/environment';

/**
 * Backend Ride interface (matches backend entity)
 */
interface BackendRide {
  id: string; // ← Changé de number à string
  driverId: string; // ← Changé de number à string
  departureCity: { name: string; postalCode?: string };
  destinationCity: { name: string; postalCode?: string };
  departureDate: string;
  availableSeats: number;
 
  status: string;
  createdAt: string;
  updatedAt?: string;
}

/**
 * Backend CreateRideRequest
 */
interface BackendCreateRideRequest {
  departureCity: { name: string; postalCode?: string };
  destinationCity: { name: string; postalCode?: string };
  departureDate: string;
  availableSeats: number;

  driverId: string; // ← Changé de number à string
}

/**
 * Ride Service
 * 
 * Handles ride API calls only.
 * Does NOT manage state - use RidesStore for state management.
 */
@Injectable({
  providedIn: 'root'
})
export class RideService {
  private apiUrl = `${environment.apiUrl}${environment.services.rides}`;

  constructor(private http: HttpClient) { }

  /**
   * Get all available rides
   * Makes API call to fetch all rides
   */
  getAllRides(): Observable<Ride[]> {
    return this.http.get<BackendRide[]>(this.apiUrl).pipe(
      map(rides => rides.map(r => this.mapBackendRideToFrontend(r))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch rides'));
      })
    );
  }

  /**
   * Get rides with filters
   * Makes API call to search rides with filters
   */
  searchRides(filters: {
    departureCity?: string;
    destinationCity?: string;
    date?: Date;
    gender?: Gender;
    passengerGender?: Gender;
  }): Observable<Ride[]> {
    let params = new HttpParams();
    if (filters.departureCity) {
      params = params.set('departureCity', filters.departureCity);
    }
    if (filters.destinationCity) {
      params = params.set('destinationCity', filters.destinationCity);
    }
    if (filters.date) {
      params = params.set('date', filters.date.toISOString().split('T')[0]);
    }

    return this.http.get<BackendRide[]>(`${this.apiUrl}/search`, { params }).pipe(
      map(rides => {
        let filteredRides = rides.map(r => this.mapBackendRideToFrontend(r));

        // Client-side filtering for gender (backend doesn't support this yet)
        if (filters.passengerGender === Gender.FEMALE && filters.gender) {
          // This would require fetching driver info, skip for now
        }

        return filteredRides.filter(r => r.availableSeats > 0 && r.status === RideStatus.SCHEDULED);
      }),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to search rides'));
      })
    );
  }

  /**
   * Get ride by ID
   */
  getRideById(id: string): Observable<Ride | undefined> {
    return this.http.get<BackendRide>(`${this.apiUrl}/${id}`).pipe(
      map(ride => this.mapBackendRideToFrontend(ride)),
      catchError(error => {
        if (error.status === 404) {
          return of(undefined);
        }
        return throwError(() => new Error(error.error?.message || 'Failed to fetch ride'));
      })
    );
  }

  /**
   * Create a new ride
   */
  createRide(rideData: Partial<Ride>): Observable<Ride> {
  const request: BackendCreateRideRequest = {
    departureCity: { name: rideData.departureCity || '' },
    destinationCity: { name: rideData.destinationCity || '' },
    departureDate: rideData.departureDate ? 
      rideData.departureDate.toISOString().split('T')[0] : 
      new Date().toISOString().split('T')[0],
    availableSeats: rideData.availableSeats || 0,
    
    driverId: rideData.driverId || '' // ← Changé de parseInt() à string
  };

  return this.http.post<BackendRide>(`${this.apiUrl}/create`, request).pipe(
    map(ride => this.mapBackendRideToFrontend(ride)),
    catchError(error => {
      return throwError(() => new Error(error.error?.message || 'Failed to create ride'));
    })
  );
}

  /**
   * Get rides by driver ID
   */
  getDriverRides(driverId: string): Observable<Ride[]> {
    return this.http.get<BackendRide[]>(`${this.apiUrl}/driver/${driverId}`).pipe(
      map(rides => rides.map(r => this.mapBackendRideToFrontend(r))),
      catchError(error => {
        return throwError(() => new Error(error.error?.message || 'Failed to fetch driver rides'));
      })
    );
  }

  /**
   * Map backend Ride to frontend Ride
   */
  private mapBackendRideToFrontend(backendRide: BackendRide): Ride {
  return {
    id: backendRide.id, // ← Supprimé .toString()
    driverId: backendRide.driverId, // ← Supprimé .toString()
    driver: {} as User,
    departureCity: backendRide.departureCity.name,
    destinationCity: backendRide.destinationCity.name,
    departureDate: new Date(backendRide.departureDate),
    departureTime: '08:00', // ← Valeur par défaut
    availableSeats: backendRide.availableSeats,
    totalSeats: backendRide.availableSeats,
    status: backendRide.status as RideStatus,
    passengers: [],
    createdAt: new Date(backendRide.createdAt)
  };
}
}

