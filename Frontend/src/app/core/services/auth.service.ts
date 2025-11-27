import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError, switchMap } from 'rxjs';
import { User, UserRole, Gender, AuthResponse } from '../models/user.model';
import { environment } from '../../../environments/environment';

/**
 * Backend AppUser interface (matches backend entity)
 */
interface BackendAppUser {
  id: string; // Changé de number à string (MongoDB ObjectId)
  email: string;
  password?: string;
  phoneNumber: string;
  gender: 'MALE' | 'FEMALE';
  userType: 'PASSENGER' | 'DRIVER' | 'ADMIN'; // 'Admin' → 'ADMIN'
  createdAt: string;
  updatedAt?: string;
  isBanned?: boolean;
}

/**
 * Authentication Service
 *
 * Handles authentication API calls only.
 * Does NOT manage state - use AuthStore for state management.
 */
@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = `${environment.apiUrl}${environment.services.auth}`;

    constructor(private http: HttpClient) {}

    /**
     * Login user with email and password
     * Makes API call to authenticate user
     */
    login(email: string, password: string): Observable<AuthResponse> {
  return this.http.post<any>(
    `${this.apiUrl}/authenticate`,
    { email, password }
  ).pipe(
    map((response: any) => {
      if (response.status !== 'success') {
        throw new Error('Authentication failed');
      }
      const user = this.mapBackendUserToFrontend(response.user);
      return {
        user,
        token: response.token
      };
    }),
    catchError(error => {
      return throwError(() => new Error(error.error?.message || 'Login failed'));
    })
  );
}



    /**
     * Register new user
     * Makes API call to register a new user
     */
    register(userData: any): Observable<AuthResponse> {
        const request = {
            email: userData.email,
            password: userData.password,
            phoneNumber: userData.phone,
            gender: userData.gender || 'MALE',
            userType: userData.role === UserRole.ADMIN ? 'Admin' : userData.role,
            licenseNumber: userData.cin || '',
            vehicleNumber: userData.vehicle?.model || '',
            vehiclePlate: userData.vehicle?.licensePlate || '',
            preferredPaymentMethod: ''
        };

        return this.http.post<BackendAppUser>(`${this.apiUrl}/createAccount`, request).pipe(
            map(backendUser => {
                const user = this.mapBackendUserToFrontend(backendUser);
                return {
                    user,
                    token: `token-${user.id}`
                };
            }),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Registration failed'));
            })
        );
    }

    /**
     * Get user by email
     */
    getUserByEmail(email: string): Observable<User> {
        return this.http.get<BackendAppUser>(`${this.apiUrl}/users/email/${encodeURIComponent(email)}`).pipe(
            map(backendUser => this.mapBackendUserToFrontend(backendUser)),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Failed to get user'));
            })
        );
    }

    /**
     * Get user by ID
     */
    getUserById(id: string): Observable<User> {
        return this.http.get<BackendAppUser>(`${this.apiUrl}/users/${id}`).pipe(
            map(backendUser => this.mapBackendUserToFrontend(backendUser)),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Failed to get user'));
            })
        );
    }

    /**
     * Get all users (Admin only)
     */
    getAllUsers(): Observable<User[]> {
        return this.http.get<BackendAppUser[]>(`${this.apiUrl}/users`).pipe(
            map(backendUsers => backendUsers.map(u => this.mapBackendUserToFrontend(u))),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Failed to get users'));
            })
        );
    }

    /**
     * Ban user (Admin only)
     */
    banUser(userId: string): Observable<User> {
        return this.http.put<BackendAppUser>(`${this.apiUrl}/users/${userId}/ban`, {}).pipe(
            map(backendUser => this.mapBackendUserToFrontend(backendUser)),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Failed to ban user'));
            })
        );
    }

    /**
     * Unban user (Admin only)
     */
    unbanUser(userId: string): Observable<User> {
        return this.http.put<BackendAppUser>(`${this.apiUrl}/users/${userId}/unban`, {}).pipe(
            map(backendUser => this.mapBackendUserToFrontend(backendUser)),
            catchError(error => {
                return throwError(() => new Error(error.error?.message || 'Failed to unban user'));
            })
        );
    }

    /**
     * Map backend AppUser to frontend User
     */
  private mapBackendUserToFrontend(backendUser: BackendAppUser): User {
  return {
    id: backendUser.id, // Changé de .toString() car MongoDB utilise String
    name: backendUser.email.split('@')[0],
    email: backendUser.email,
    phone: backendUser.phoneNumber,
    role: backendUser.userType === 'ADMIN' ? UserRole.ADMIN :
          backendUser.userType === 'DRIVER' ? UserRole.DRIVER : UserRole.PASSENGER,
    gender: backendUser.gender === 'MALE' ? Gender.MALE : Gender.FEMALE,
    createdAt: new Date(backendUser.createdAt),
    isBanned: backendUser.isBanned || false
  };
}
}