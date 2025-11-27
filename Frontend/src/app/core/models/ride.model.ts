import { User, UserRole } from './user.model';

/**
 * Ride status enum
 */
export enum RideStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

/**
 * Ride model interface
 */
export interface Ride {
  id: string;
  driverId: string;
  driver?: User; // Optional - may not be populated from backend
  departureCity: string;
  destinationCity: string;
  departureDate: Date;
  departureTime: string;
  availableSeats: number;
  totalSeats: number;
  status: RideStatus;
  passengers?: Booking[]; // Optional - may not be populated from backend
  createdAt: Date;
}

/**
 * Booking status enum
 */
export enum BookingStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

/**
 * Booking model interface
 */
export interface Booking {
  id: string;
  rideId: string;
  ride?: Ride; // Optional - may not be populated from backend
  passengerId: string;
  passenger?: User; // Optional - may not be populated from backend
  status: BookingStatus;
  seatsRequested: number;
  createdAt: Date;
}

