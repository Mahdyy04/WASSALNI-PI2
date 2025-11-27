import { Injectable, signal, computed } from '@angular/core';
import { User, Gender } from '../models/user.model';

/**
 * User Store
 * 
 * Manages user profile and preferences state including:
 * - User profile information
 * - User settings
 * - User preferences
 * 
 * Uses Angular Signals for reactive state management.
 * 
 * Note: This store is separate from AuthStore to handle user-specific
 * profile data that may differ from authentication state.
 */
@Injectable({
  providedIn: 'root'
})
export class UserStore {
  // ============================================================================
  // Private State Signals
  // ============================================================================

  /**
   * Private signal storing user profile data
   * This is separate from auth state to allow for profile editing
   */
  private readonly userProfileSignal = signal<User | null>(null);

  /**
   * Private signal storing user preferences
   * Can include UI preferences, notification settings, etc.
   */
  private readonly preferencesSignal = signal<UserPreferences>({
    theme: 'light',
    notificationsEnabled: true,
    emailNotifications: true,
    preferredGender: null,
    preferredSeats: 1
  });

  /**
   * Private signal storing user settings
   * Application-wide settings for the user
   */
  private readonly settingsSignal = signal<UserSettings>({
    language: 'en',
    currency: 'TND',
    timezone: 'Africa/Tunis'
  });

  // ============================================================================
  // Public Readonly Computed Signals
  // ============================================================================

  /**
   * Current user profile (readonly)
   * Returns null if no profile is loaded
   */
  public readonly userProfile = computed(() => this.userProfileSignal());

  /**
   * User preferences (readonly)
   */
  public readonly preferences = computed(() => this.preferencesSignal());

  /**
   * User settings (readonly)
   */
  public readonly settings = computed(() => this.settingsSignal());

  /**
   * User rating (readonly)
   * Computed from user profile
   */
  public readonly rating = computed(() => 
    this.userProfileSignal()?.rating ?? 0
  );

  /**
   * Total rides count (readonly)
   * Computed from user profile
   */
  public readonly totalRides = computed(() => 
    this.userProfileSignal()?.totalRides ?? 0
  );

  // ============================================================================
  // Constructor
  // ============================================================================

  constructor() {
    // Initialize preferences from localStorage if available
    this.initializePreferencesFromStorage();
  }

  // ============================================================================
  // State Update Methods (Setters)
  // ============================================================================

  /**
   * Set user profile
   * Called when user profile is loaded or updated
   * 
   * @param profile The user profile to store
   */
  setUserProfile(profile: User): void {
    this.userProfileSignal.set(profile);
  }

  /**
   * Update user profile partially
   * Used for updating specific profile fields
   * 
   * @param updates Partial user object with fields to update
   */
  updateUserProfile(updates: Partial<User>): void {
    const currentProfile = this.userProfileSignal();
    if (currentProfile) {
      const updatedProfile: User = { ...currentProfile, ...updates };
      this.userProfileSignal.set(updatedProfile);
    }
  }

  /**
   * Set user preferences
   * 
   * @param preferences The user preferences to store
   */
  setPreferences(preferences: Partial<UserPreferences>): void {
    const currentPreferences = this.preferencesSignal();
    this.preferencesSignal.set({ ...currentPreferences, ...preferences });
    this.savePreferencesToStorage();
  }

  /**
   * Set user settings
   * 
   * @param settings The user settings to store
   */
  setSettings(settings: Partial<UserSettings>): void {
    const currentSettings = this.settingsSignal();
    this.settingsSignal.set({ ...currentSettings, ...settings });
  }

  /**
   * Clear user profile
   * Called on logout or when profile needs to be cleared
   */
  clearProfile(): void {
    this.userProfileSignal.set(null);
  }

  // ============================================================================
  // Utility Methods
  // ============================================================================

  /**
   * Initialize preferences from localStorage
   */
  private initializePreferencesFromStorage(): void {
    const stored = localStorage.getItem('userPreferences');
    if (stored) {
      try {
        const preferences: UserPreferences = JSON.parse(stored);
        this.preferencesSignal.set({ ...this.preferencesSignal(), ...preferences });
      } catch (error) {
        console.error('Error parsing stored preferences:', error);
      }
    }
  }

  /**
   * Save preferences to localStorage
   */
  private savePreferencesToStorage(): void {
    localStorage.setItem('userPreferences', JSON.stringify(this.preferencesSignal()));
  }
}

// ============================================================================
// Type Definitions
// ============================================================================

/**
 * User preferences interface
 * Defines user-specific preferences and UI settings
 */
export interface UserPreferences {
  theme: 'light' | 'dark' | 'auto';
  notificationsEnabled: boolean;
  emailNotifications: boolean;
  preferredGender: Gender | null;
  preferredSeats: number;
}

/**
 * User settings interface
 * Defines application-wide settings for the user
 */
export interface UserSettings {
  language: string;
  currency: string;
  timezone: string;
}

