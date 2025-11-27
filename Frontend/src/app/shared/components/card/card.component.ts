import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Reusable card component
 * Provides consistent card styling across the application
 */
@Component({
  selector: 'app-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="getCardClasses()">
      @if (title) {
        <div class="mb-4">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            {{ title }}
          </h3>
          @if (subtitle) {
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ subtitle }}</p>
          }
        </div>
      }
      <ng-content></ng-content>
    </div>
  `,
  styles: []
})
export class CardComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() padding: 'sm' | 'md' | 'lg' = 'md';
  @Input() shadow: 'sm' | 'md' | 'lg' = 'md';

  getCardClasses(): string {
    const baseClasses = 'card';
    
    const paddingClasses = {
      sm: 'p-4',
      md: 'p-6',
      lg: 'p-8'
    };

    const shadowClasses = {
      sm: 'shadow-sm',
      md: 'shadow-md',
      lg: 'shadow-lg'
    };

    return `${baseClasses} ${paddingClasses[this.padding]} ${shadowClasses[this.shadow]}`;
  }
}

