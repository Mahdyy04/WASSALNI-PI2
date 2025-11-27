import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReviewService } from '../../../core/services/review.service';
import { Review } from '../../../core/models/review.model';
import { CardComponent } from '../../../shared/components/card/card.component';

@Component({
    selector: 'app-review-list',
    standalone: true,
    imports: [CommonModule, CardComponent],
    template: `
    <div class="space-y-4">
      <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Reviews</h3>
      
      @if (loading) {
        <div class="flex justify-center py-4">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
        </div>
      } @else if (error) {
        <div class="p-4 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 rounded-lg">
          {{ error }}
        </div>
      } @else if (reviews.length === 0) {
        <p class="text-gray-500 dark:text-gray-400 text-center py-4">No reviews yet.</p>
      } @else {
        <div class="grid gap-4">
          @for (review of reviews; track review.id) {
            <app-card class="hover:shadow-md transition-shadow">
              <div class="flex justify-between items-start">
                <div>
                  <div class="flex items-center mb-2">
                    <div class="flex text-yellow-400">
                      @for (star of [1, 2, 3, 4, 5]; track star) {
                        <svg class="w-5 h-5" [class.text-gray-300]="star > review.rating" fill="currentColor" viewBox="0 0 20 20">
                          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                        </svg>
                      }
                    </div>
                    <span class="ml-2 text-sm text-gray-600 dark:text-gray-400">
                      {{ review.createdAt | date:'mediumDate' }}
                    </span>
                  </div>
                  <p class="text-gray-700 dark:text-gray-300">{{ review.comment }}</p>
                </div>
              </div>
            </app-card>
          }
        </div>
      }
    </div>
  `,
    styles: []
})
export class ReviewListComponent implements OnInit, OnChanges {
    @Input() userId: string | undefined;
    reviews: Review[] = [];
    loading = false;
    error = '';

    constructor(private reviewService: ReviewService) { }

    ngOnInit(): void {
        if (this.userId) {
            this.loadReviews();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['userId'] && !changes['userId'].firstChange && this.userId) {
            this.loadReviews();
        }
    }

    loadReviews(): void {
        if (!this.userId) return;

        this.loading = true;
        this.error = '';

        this.reviewService.getUserReviews(this.userId).subscribe({
            next: (reviews) => {
                this.reviews = reviews;
                this.loading = false;
            },
            error: (err) => {
                console.error('Error loading reviews:', err);
                this.error = 'Failed to load reviews';
                this.loading = false;
            }
        });
    }
}
