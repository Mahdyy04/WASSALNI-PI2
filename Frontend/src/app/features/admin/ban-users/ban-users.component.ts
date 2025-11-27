import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { User, UserRole } from '../../../core/models/user.model';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';

/**
 * Admin Ban Users component
 * Allows admins to ban/unban users from the platform
 */
@Component({
  selector: 'app-admin-ban-users',
  standalone: true,
  imports: [CommonModule, DatePipe, CardComponent, ButtonComponent],
  template: `
    <div>
      <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-8">Ban Users</h1>

      <div class="mb-6">
        <p class="text-gray-600 dark:text-gray-400">
          Manage user bans and restrictions
        </p>
      </div>

      <div class="space-y-4">
        @for (user of users; track user.id) {
          <app-card>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center space-x-4 mb-4">
                  <div class="w-12 h-12 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold">
                    {{ getInitials(user.name) }}
                  </div>
                  <div>
                    <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
                      {{ user.name }}
                    </h3>
                    <p class="text-sm text-gray-600 dark:text-gray-400">{{ user.email }}</p>
                  </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Role</p>
                    <span [class]="getRoleClass(user.role)">
                      {{ user.role }}
                    </span>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Status</p>
                    <span [class]="user.isBanned ? 'px-3 py-1 rounded-full text-sm font-medium bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400' : 'px-3 py-1 rounded-full text-sm font-medium bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400'">
                      {{ user.isBanned ? 'Banned' : 'Active' }}
                    </span>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Joined</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ user.createdAt | date:'short' }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="ml-4">
                @if (user.isBanned) {
                  <app-button
                      variant="primary"
                      size="sm"
                      (onClick)="unbanUser(user.id)"
                      [loading]="loadingUsers.has(user.id)"
                  >
                    Unban
                  </app-button>
                } @else {
                  <app-button
                      variant="danger"
                      size="sm"
                      (onClick)="banUser(user.id)"
                      [loading]="loadingUsers.has(user.id)"
                  >
                    Ban
                  </app-button>
                }
              </div>
            </div>
          </app-card>
        } @empty {
          <app-card>
            <div class="text-center py-12">
              <p class="text-gray-600 dark:text-gray-400">No users found</p>
            </div>
          </app-card>
        }
      </div>
    </div>
  `,
  styles: []
})
export class AdminBanUsersComponent implements OnInit {
  users: User[] = [];
  loadingUsers = new Set<string>();

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (error) => {
        console.error('Error loading users:', error);
      }
    });
  }

  banUser(userId: string): void {
    if (!confirm('Are you sure you want to ban this user?')) {
      return;
    }

    this.loadingUsers.add(userId);
    this.authService.banUser(userId).subscribe({
      next: (updatedUser) => {
        this.loadingUsers.delete(userId);
        // Update user in list
        const index = this.users.findIndex(u => u.id === userId);
        if (index !== -1) {
          this.users[index] = updatedUser;
        }
      },
      error: (error) => {
        this.loadingUsers.delete(userId);
        console.error('Error banning user:', error);
        alert('Failed to ban user. Please try again.');
      }
    });
  }

  unbanUser(userId: string): void {
    if (!confirm('Are you sure you want to unban this user?')) {
      return;
    }

    this.loadingUsers.add(userId);
    this.authService.unbanUser(userId).subscribe({
      next: (updatedUser) => {
        this.loadingUsers.delete(userId);
        // Update user in list
        const index = this.users.findIndex(u => u.id === userId);
        if (index !== -1) {
          this.users[index] = updatedUser;
        }
      },
      error: (error) => {
        this.loadingUsers.delete(userId);
        console.error('Error unbanning user:', error);
        alert('Failed to unban user. Please try again.');
      }
    });
  }

  getRoleClass(role: UserRole): string {
    const baseClasses = 'px-3 py-1 rounded-full text-sm font-medium';
    switch (role) {
      case UserRole.ADMIN:
        return `${baseClasses} bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-400`;
      case UserRole.DRIVER:
        return `${baseClasses} bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400`;
      case UserRole.PASSENGER:
        return `${baseClasses} bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400`;
      default:
        return `${baseClasses} bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-300`;
    }
  }

  getInitials(name: string): string {
    return name
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);
  }
}