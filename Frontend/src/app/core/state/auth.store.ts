import { Injectable, signal, computed, effect } from '@angular/core';
import { Router } from '@angular/router';
import { User, UserRole, AuthResponse } from '../models/user.model';

/**
 * Authentication Store
 * 
 * Manages authentication state including:
 * - Current logged-in user
 * - User roles and permissions
 * - Authentication status
 * 
 * Uses Angular Signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthStore {
  // ============================================================================
  // Private State Signals
  // ============================================================================

  /**
   * Private signal storing the current authenticated user
   * null when user is not authenticated
   */
  private readonly currentUserSignal = signal<User | null>(null);

  /**
   * Private signal storing the authentication token
   */
  private readonly tokenSignal = signal<string | null>(null);

  // ============================================================================
  // Public Readonly Computed Signals
  // ============================================================================

  /**
   * Current authenticated user (readonly)
   * Returns null if no user is authenticated
   */
  public readonly currentUser = computed(() => this.currentUserSignal());

  /**
   * Authentication status (readonly)
   * Returns true if user is authenticated, false otherwise
   */
  public readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  /**
   * Admin role check (readonly)
   * Returns true if current user has ADMIN role
   */
  public readonly isAdmin = computed(() => 
    this.currentUserSignal()?.role === UserRole.ADMIN
  );

  /**
   * Driver role check (readonly)
   * Returns true if current user has DRIVER role
   */
  public readonly isDriver = computed(() => 
    this.currentUserSignal()?.role === UserRole.DRIVER
  );

  /**
   * Passenger role check (readonly)
   * Returns true if current user has PASSENGER role
   */
  public readonly isPassenger = computed(() => 
    this.currentUserSignal()?.role === UserRole.PASSENGER
  );

  /**
   * Current authentication token (readonly)
   * Returns null if no token is available
   */
  public readonly token = computed(() => this.tokenSignal());

  // ============================================================================
  // Constructor
  // ============================================================================

  constructor(private router: Router) {
    // Initialize from localStorage if available
    this.initializeFromStorage();

    // Effect to persist user to localStorage when it changes
    effect(() => {
      const user = this.currentUserSignal();
      const token = this.tokenSignal();

      if (user) {
        localStorage.setItem('currentUser', JSON.stringify(user));
        if (token) {
          localStorage.setItem('token', token);
        }
      } else {
        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
      }
    });
  }

  // ============================================================================
  // State Update Methods (Setters)
  // ============================================================================

  /**
   * Set authenticated user and token
   * Called after successful login or registration
   * 
   * @param user The authenticated user
   * @param token The authentication token
   */
  setAuthenticatedUser(user: User, token: string): void {
    this.currentUserSignal.set(user);
    this.tokenSignal.set(token);
  }

  /**
   * Update user role
   * Used when user selects a role during role selection flow
   * 
   * @param role The new role to assign to the user
   */
  updateUserRole(role: UserRole): void {
    const currentUser = this.currentUserSignal();
    if (currentUser) {
      const updatedUser: User = { ...currentUser, role };
      this.currentUserSignal.set(updatedUser);
    }
  }

  /**
   * Update current user information
   * Used when user profile is updated
   * 
   * @param user The updated user object
   */
  updateUser(user: User): void {
    this.currentUserSignal.set(user);
  }

  /**
   * Clear authentication state
   * Called on logout to reset all auth-related state
   */
  logout(): void {
    this.currentUserSignal.set(null);
    this.tokenSignal.set(null);
    this.router.navigate(['/auth/login']);
  }

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Initialize store from localStorage
   * Restores user session on app initialization
   */
  private initializeFromStorage(): void {
    const storedUser = localStorage.getItem('currentUser');
    const storedToken = localStorage.getItem('token');

    if (storedUser) {
      try {
        const user: User = JSON.parse(storedUser);
        // Convert date strings back to Date objects
        if (user.createdAt) {
          user.createdAt = new Date(user.createdAt);
        }
        this.currentUserSignal.set(user);
        if (storedToken) {
          this.tokenSignal.set(storedToken);
        }
      } catch (error) {
        console.error('Error parsing stored user:', error);
        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
      }
    }
  }

  /**
   * Get authentication token
   * @returns The current authentication token or null
   */
  getToken(): string | null {
    return this.tokenSignal();
  }
}

