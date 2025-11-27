import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../../core/state/auth.store';
import { NotificationsStore } from '../../../core/state/notifications.store';

/**
 * Navigation bar component
 * Shows different navigation based on user role
 * Includes notifications badge and dark mode toggle
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="bg-white dark:bg-gray-800 shadow-md border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <a routerLink="/" class="flex items-center space-x-2">
              <svg class="w-8 h-8 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
              </svg>
              <span class="text-xl font-bold text-gray-900 dark:text-gray-100">Wassalni</span>
            </a>

            @if (authStore.isAuthenticated() && !router.url.includes('/auth/role-selection')) {
              <div class="ml-10 flex items-center space-x-4">
                @if (authStore.isPassenger()) {
                  <a routerLink="/passenger/dashboard" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    Dashboard
                  </a>
                  <a routerLink="/passenger/bookings" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    My Bookings
                  </a>
                }
                @if (authStore.isDriver()) {
                  <a routerLink="/driver/dashboard" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    Dashboard
                  </a>
                  <a routerLink="/driver/rides" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    My Rides
                  </a>
                  <a routerLink="/driver/bookings" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    Bookings
                  </a>
                }
                @if (authStore.isAdmin()) {
                  <a routerLink="/admin/dashboard" routerLinkActive="text-primary-600" 
                     class="px-3 py-2 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400">
                    Admin Dashboard
                  </a>
                }
              </div>
            }
          </div>

          <div class="flex items-center space-x-4">
            @if (authStore.isAuthenticated()) {
              <!-- Notifications -->
              <button 
                (click)="toggleNotifications()"
                class="relative p-2 text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400"
              >
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                @if (notificationsStore.unreadCount() > 0) {
                  <span class="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-600 ring-2 ring-white dark:ring-gray-800"></span>
                }
              </button>

              <!-- User menu -->
              <div class="relative">
                <button 
                  (click)="toggleUserMenu()"
                  class="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700"
                >
                  <div class="w-8 h-8 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold">
                    {{ getInitials() }}
                  </div>
                  <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
                    {{ authStore.currentUser()?.name }}
                  </span>
                </button>

                @if (showUserMenu()) {
                  <div class="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-800 rounded-md shadow-lg py-1 z-50 border border-gray-200 dark:border-gray-700">
                    <a routerLink="/profile" class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                      Profile
                    </a>
                    <button (click)="logout()" class="block w-full text-left px-4 py-2 text-sm text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700">
                      Logout
                    </button>
                  </div>
                }
              </div>
            } @else {
              <a routerLink="/auth/login" class="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:text-primary-600">
                Login
              </a>
              <a routerLink="/auth/signup" class="px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700">
                Sign Up
              </a>
            }

            <!-- Dark mode toggle -->
            <button 
              (click)="toggleDarkMode()"
              class="p-2 text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400"
            >
              @if (isDarkMode()) {
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              } @else {
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                </svg>
              }
            </button>
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: []
})
export class NavbarComponent {
  showUserMenuSignal = signal(false);
  isDarkModeSignal = signal(false);

  showUserMenu = computed(() => this.showUserMenuSignal());
  isDarkMode = computed(() => this.isDarkModeSignal());

  constructor(
    public authStore: AuthStore,
    public notificationsStore: NotificationsStore,
    public router: Router
  ) {
    // Check dark mode from localStorage
    const darkMode = localStorage.getItem('darkMode') === 'true';
    this.isDarkModeSignal.set(darkMode);
    if (darkMode) {
      document.documentElement.classList.add('dark');
    }
  }

  toggleUserMenu(): void {
    this.showUserMenuSignal.update(v => !v);
  }

  toggleDarkMode(): void {
    const newValue = !this.isDarkModeSignal();
    this.isDarkModeSignal.set(newValue);
    localStorage.setItem('darkMode', newValue.toString());
    if (newValue) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  toggleNotifications(): void {
    // TODO: Open notifications panel
    console.log('Toggle notifications');
  }

  logout(): void {
    this.authStore.logout();
    this.showUserMenuSignal.set(false);
  }

  getInitials(): string {
    const user = this.authStore.currentUser();
    if (!user) return 'U';
    return user.name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }
}

