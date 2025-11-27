import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

/**
 * Sidebar navigation component
 * Used primarily in admin dashboard
 */
export interface SidebarItem {
  label: string;
  route: string;
  icon?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="w-64 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 min-h-screen">
      <div class="p-6">
        <h2 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-6">
          {{ title }}
        </h2>
        <nav class="space-y-2">
          @for (item of items; track item.route) {
            <a
              [routerLink]="item.route"
              routerLinkActive="bg-primary-50 dark:bg-primary-900/20 text-primary-600 dark:text-primary-400"
              [routerLinkActiveOptions]="{ exact: false }"
              class="flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors duration-200"
            >
              @if (item.icon) {
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" [attr.d]="item.icon" />
                </svg>
              }
              <span class="font-medium">{{ item.label }}</span>
            </a>
          }
        </nav>
      </div>
    </aside>
  `,
  styles: []
})
export class SidebarComponent {
  @Input() title = 'Navigation';
  @Input() items: SidebarItem[] = [];
}

