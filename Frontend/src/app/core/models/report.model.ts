import { User } from './user.model';

/**
 * Report status enum
 */
export enum ReportStatus {
  PENDING = 'PENDING',
  REVIEWED = 'REVIEWED',
  RESOLVED = 'RESOLVED',
  DISMISSED = 'DISMISSED'
}

/**
 * Report reason enum
 */
export enum ReportReason {
  INAPPROPRIATE_BEHAVIOR = 'INAPPROPRIATE_BEHAVIOR',
  NO_SHOW = 'NO_SHOW',
  UNSAFE_DRIVING = 'UNSAFE_DRIVING',
  OTHER = 'OTHER'
}

/**
 * Report model interface
 */
export interface Report {
  id: string;
  reporterId: string;
  reporter: User;
  reportedUserId: string;
  reportedUser: User;
  reason: ReportReason;
  description: string;
  status: ReportStatus;
  rideId?: string;
  createdAt: Date;
}

