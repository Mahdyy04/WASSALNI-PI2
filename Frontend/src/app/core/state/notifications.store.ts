import { Injectable, signal, computed, effect, inject } from '@angular/core';
import { Notification, NotificationType } from '../models/notification.model';
import { AuthStore } from './auth.store';

/**
 * Notifications Store
 * 
 * Manages notification-related state including:
 * - List of all notifications
 * - Unread notifications count
 * - Notification filtering
 * 
 * Uses Angular Signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationsStore {
  private authStore = inject(AuthStore);
  // ============================================================================
  // Private State Signals
  // ============================================================================

  /**
   * Private signal storing all notifications
   * This is the source of truth for notifications data
   */
  private readonly notificationsSignal = signal<Notification[]>([]);

  /**
   * Private signal storing the current user ID
   * Used for filtering user-specific notifications
   */
  private readonly currentUserIdSignal = signal<string | null>(null);

  // ============================================================================
  // Public Readonly Computed Signals
  // ============================================================================

  /**
   * All notifications (readonly)
   */
  public readonly notifications = computed(() => this.notificationsSignal());

  /**
   * Current user ID (readonly)
   */
  public readonly currentUserId = computed(() => this.currentUserIdSignal());

  /**
   * Notifications for current user (readonly)
   * Automatically filtered by current user ID
   */
  public readonly userNotifications = computed(() => {
    const notifications = this.notificationsSignal();
    const userId = this.currentUserIdSignal();

    if (!userId) {
      return [];
    }

    return notifications
      .filter(notification => notification.userId === userId)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
  });

  /**
   * Unread notifications for current user (readonly)
   */
  public readonly unreadNotifications = computed(() =>
    this.userNotifications().filter(notification => !notification.read)
  );

  /**
   * Read notifications for current user (readonly)
   */
  public readonly readNotifications = computed(() =>
    this.userNotifications().filter(notification => notification.read)
  );

  /**
   * Unread notifications count (readonly)
   */
  public readonly unreadCount = computed(() => 
    this.unreadNotifications().length
  );

  /**
   * Notifications by type (readonly)
   * Helper computed that requires notification type
   * Usage: notificationsStore.notificationsByType(NotificationType.BOOKING_REQUEST)()
   */
  public readonly notificationsByType = (type: NotificationType) =>
    computed(() =>
      this.userNotifications().filter(notification => notification.type === type)
    );

  // ============================================================================
  // Constructor
  // ============================================================================

  constructor() {
    // Sync current user ID with auth store
    effect(() => {
      const currentUser = this.authStore.currentUser();
      this.setCurrentUserId(currentUser?.id || null);
    });
  }

  // ============================================================================
  // State Update Methods (Setters)
  // ============================================================================

  /**
   * Set all notifications
   * Called when notifications are loaded from API
   * 
   * @param notifications Array of notifications to store
   */
  setNotifications(notifications: Notification[]): void {
    this.notificationsSignal.set(notifications);
  }

  /**
   * Add a new notification
   * Called when a new notification is created
   * 
   * @param notification The new notification to add
   */
  addNotification(notification: Notification): void {
    this.notificationsSignal.update(notifications => [...notifications, notification]);
  }

  /**
   * Update an existing notification
   * Called when notification properties are updated
   * 
   * @param notificationId The ID of the notification to update
   * @param updates Partial notification object with fields to update
   */
  updateNotification(notificationId: string, updates: Partial<Notification>): void {
    this.notificationsSignal.update(notifications =>
      notifications.map(notification =>
        notification.id === notificationId
          ? { ...notification, ...updates }
          : notification
      )
    );
  }

  /**
   * Mark notification as read
   * Convenience method for marking a single notification as read
   * 
   * @param notificationId The ID of the notification to mark as read
   */
  markAsRead(notificationId: string): void {
    this.updateNotification(notificationId, { read: true });
  }

  /**
   * Mark all notifications as read
   * Marks all notifications for the current user as read
   */
  markAllAsRead(): void {
    const userId = this.currentUserIdSignal();
    if (!userId) {
      return;
    }

    this.notificationsSignal.update(notifications =>
      notifications.map(notification =>
        notification.userId === userId && !notification.read
          ? { ...notification, read: true }
          : notification
      )
    );
  }

  /**
   * Remove a notification
   * Called when a notification is deleted
   * 
   * @param notificationId The ID of the notification to remove
   */
  removeNotification(notificationId: string): void {
    this.notificationsSignal.update(notifications =>
      notifications.filter(notification => notification.id !== notificationId)
    );
  }

  /**
   * Set current user ID
   * Called when user logs in to filter notifications
   * 
   * @param userId The current user ID, or null to clear
   */
  setCurrentUserId(userId: string | null): void {
    this.currentUserIdSignal.set(userId);
  }

  /**
   * Clear all notifications
   * Called on logout or when notifications need to be cleared
   */
  clearNotifications(): void {
    this.notificationsSignal.set([]);
    this.currentUserIdSignal.set(null);
  }
}

