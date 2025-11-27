import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Notification, NotificationType } from '../models/notification.model';

/**
 * Notification Service
 * 
 * Handles notification API calls only.
 * Does NOT manage state - use NotificationsStore for state management.
 * 
 * NOTE: Currently uses mock data as there is no notification microservice in the backend.
 * In production, this would make HTTP calls to a notification service endpoint.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  // Mock notifications data storage
  // TODO: Replace with HTTP calls when notification microservice is implemented
  private mockNotifications: Notification[] = [];

  constructor() {}

  /**
   * Get notifications for a user
   * Makes API call to fetch user notifications
   * 
   * @param userId The user ID
   * @returns Observable with array of user notifications
   */
  getUserNotifications(userId: string): Observable<Notification[]> {
    const notifications = this.mockNotifications
      .filter(n => n.userId === userId)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
    return of(notifications).pipe(delay(200));
  }

  /**
   * Get all notifications
   * Makes API call to fetch all notifications
   * 
   * @returns Observable with array of all notifications
   */
  getAllNotifications(): Observable<Notification[]> {
    return of([...this.mockNotifications]).pipe(delay(200));
  }

  /**
   * Create a new notification
   * Makes API call to create a notification
   * 
   * @param notification Partial notification data
   * @returns Observable with the created notification
   * 
   * Note: State management should be handled by NotificationsStore
   */
  createNotification(notification: Partial<Notification>): Observable<Notification> {
    const newNotification: Notification = {
      id: Date.now().toString(),
      userId: notification.userId || '',
      type: notification.type || NotificationType.BOOKING_REQUEST,
      title: notification.title || '',
      message: notification.message || '',
      read: false,
      relatedId: notification.relatedId,
      createdAt: new Date()
    };

    // Mock API call - in real app, this would POST to backend
    this.mockNotifications.push(newNotification);
    return of(newNotification).pipe(delay(100));
  }

  /**
   * Mark notification as read
   * Makes API call to mark a notification as read
   * 
   * @param notificationId The notification ID
   * @returns Observable that completes when notification is marked as read
   */
  markAsRead(notificationId: string): Observable<void> {
    const notification = this.mockNotifications.find(n => n.id === notificationId);
    if (notification) {
      notification.read = true;
    }
    return of(void 0).pipe(delay(100));
  }

  /**
   * Mark all notifications as read
   * Makes API call to mark all user notifications as read
   * 
   * @param userId The user ID
   * @returns Observable that completes when all notifications are marked as read
   */
  markAllAsRead(userId: string): Observable<void> {
    this.mockNotifications.forEach(n => {
      if (n.userId === userId && !n.read) {
        n.read = true;
      }
    });
    return of(void 0).pipe(delay(100));
  }
}

