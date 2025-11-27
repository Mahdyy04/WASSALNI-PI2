import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthStore } from '../../../core/state/auth.store';
import { ReviewService } from '../../../core/services/review.service';
import { RideService } from '../../../core/services/ride.service';
import { Ride } from '../../../core/models/ride.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InputComponent } from '../../../shared/components/input/input.component';
import { CardComponent } from '../../../shared/components/card/card.component';

/**
 * Create Review component
 * Allows users to rate and review drivers or passengers after a ride
 */
@Component({
  selector: 'app-create-review',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonComponent,
    InputComponent,
    CardComponent
  ],
  template: `
    <div class="container mx-auto px-4 py-8 max-w-2xl">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Rate & Review</h1>
      </div>

      @if (ride) {
        <app-card>
          <form [formGroup]="reviewForm" (ngSubmit)="onSubmit()">
            <div class="mb-6">
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
                Rating for: <span class="font-semibold">{{ userToReview?.name }}</span>
              </p>
              
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Rating <span class="text-red-500">*</span>
              </label>
              <div class="flex space-x-2 mb-4">
                @for (star of [1, 2, 3, 4, 5]; track star) {
                  <button
                    type="button"
                    (click)="setRating(star)"
                    [class]="getStarClass(star)"
                  >
                    <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                    </svg>
                  </button>
                }
              </div>
            </div>

            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Comment
              </label>
              <textarea
                formControlName="comment"
                rows="4"
                class="input-field"
                placeholder="Share your experience..."
              ></textarea>
            </div>

            @if (errorMessage) {
              <div class="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
                <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
              </div>
            }

            <div class="flex space-x-4">
              <app-button
                type="submit"
                variant="primary"
                size="lg"
                [loading]="loading"
              >
                Submit Review
              </app-button>
              <app-button
                type="button"
                variant="secondary"
                size="lg"
                (onClick)="cancel()"
              >
                Cancel
              </app-button>
            </div>
          </form>
        </app-card>
      }
    </div>
  `,
  styles: []
})
export class CreateReviewComponent implements OnInit {
  ride: Ride | null = null;
  userToReview: any = null;
  reviewForm: FormGroup;
  selectedRating = 0;
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private authStore: AuthStore,
    private reviewService: ReviewService,
    private rideService: RideService
  ) {
    this.reviewForm = this.fb.group({
      rating: [0, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
  }

  ngOnInit(): void {
    const rideId = this.route.snapshot.paramMap.get('rideId');
    const reviewedId = this.route.snapshot.paramMap.get('reviewedId');
    
    if (rideId) {
      this.loadRide(rideId);
      if (reviewedId) {
        // Load user to review
        this.userToReview = { id: reviewedId, name: 'User' };
      }
    }
  }

  loadRide(rideId: string): void {
    this.rideService.getRideById(rideId).subscribe({
      next: (ride) => {
        this.ride = ride || null;
      },
      error: (error) => {
        console.error('Error loading ride:', error);
      }
    });
  }

  setRating(rating: number): void {
    this.selectedRating = rating;
    this.reviewForm.patchValue({ rating });
  }

  getStarClass(star: number): string {
    if (star <= this.selectedRating) {
      return 'text-yellow-400 hover:text-yellow-500';
    }
    return 'text-gray-300 dark:text-gray-600 hover:text-yellow-400';
  }

  onSubmit(): void {
    if (this.reviewForm.valid && this.selectedRating > 0) {
      this.loading = true;
      this.errorMessage = '';

      const formValue = this.reviewForm.value;
      const currentUser = this.authStore.currentUser();

      if (!currentUser || !this.ride || !this.userToReview) {
        this.errorMessage = 'Missing required information';
        this.loading = false;
        return;
      }

      const reviewData = {
        reviewerId: currentUser.id,
        reviewer: currentUser,
        reviewedId: this.userToReview.id,
        reviewed: this.userToReview,
        rideId: this.ride.id,
        rating: this.selectedRating,
        comment: formValue.comment
      };

      this.reviewService.createReview(reviewData).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/passenger/bookings']);
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.message || 'Failed to submit review';
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/passenger/bookings']);
  }
}

