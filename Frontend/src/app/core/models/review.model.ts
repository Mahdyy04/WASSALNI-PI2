import { User } from './user.model';

/**
 * Review model interface
 */
export interface Review {
  id: string;
  reviewerId: string;
  reviewer: User;
  reviewedId: string; // User being reviewed
  reviewed: User;
  rideId: string;
  rating: number; // 1-5
  comment: string;
  createdAt: Date;
}

