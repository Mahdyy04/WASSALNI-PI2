/**
 * Notification type enum
 */
export enum NotificationType {
  BOOKING_REQUEST = 'BOOKING_REQUEST',
  BOOKING_ACCEPTED = 'BOOKING_ACCEPTED',
  BOOKING_REJECTED = 'BOOKING_REJECTED',
  RIDE_CANCELLED = 'RIDE_CANCELLED',
  NEW_REVIEW = 'NEW_REVIEW',
  REPORT_SUBMITTED = 'REPORT_SUBMITTED'
}

/**
 * Notification model interface
 */
export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  read: boolean;
  relatedId?: string; // Related ride/booking ID
  createdAt: Date;
}

