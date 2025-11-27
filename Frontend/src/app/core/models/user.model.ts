/**
 * User roles in the carpooling application
 */
export enum UserRole {
  PASSENGER = 'PASSENGER',
  DRIVER = 'DRIVER',
  ADMIN = 'ADMIN'
}

/**
 * Gender enum for filtering
 */
export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}

/**
 * User model interface
 */
export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: UserRole;
  gender?: Gender;
  cin?: string; // For drivers
  vehicle?: Vehicle; // For drivers
  rating?: number;
  totalRides?: number;
  createdAt: Date;
  isBanned?: boolean; // Admin ban status
}

/**
 * Vehicle information for drivers
 */
export interface Vehicle {
  id: string;
  brand: string;
  model: string;
  licensePlate: string;
  color: string;
  seats: number;
}

/**
 * Authentication response
 */
export interface AuthResponse {
  user: User;
  token: string;
}

