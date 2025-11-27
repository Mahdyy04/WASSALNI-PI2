import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { User, UserRole } from '../../../core/models/user.model';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';

/**
 * Admin Users Management component
 * Lists all users and allows viewing their details
 */
@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, DatePipe, CardComponent, ButtonComponent],
  template: `
    <div>
      <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-8">Users Management</h1>
      
      <div class="mb-6">
        <p class="text-gray-600 dark:text-gray-400">
          Total Users: <span class="font-semibold">{{ users.length }}</span>
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
                    <p class="text-xs text-gray-500 dark:text-gray-400">Phone</p>
                    <p class="font-medium text-gray-900 dark:text-gray-100">{{ user.phone || 'N/A' }}</p>
                  </div>
                  <div>
                    <p class="text-xs text-gray-500 dark:text-gray-400">Status</p>
                    <span [class]="user.isBanned ? 'text-red-600 font-bold' : 'text-green-600 font-medium'">
                      {{ user.isBanned ? 'Banned' : 'Active' }}
                    </span>
                  </div>
                </div>

                <div class="flex justify-end">
                  @if (user.role !== 'ADMIN') {
                    <app-button
                      [variant]="user.isBanned ? 'primary' : 'danger'"
                      size="sm"
                      (onClick)="toggleBan(user)"
                    >
                      {{ user.isBanned ? 'Unban User' : 'Ban User' }}
                    </app-button>
                  }
                </div>
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
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  loading = false;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.authService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.loading = false;
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

  toggleBan(user: User): void {
    if (!confirm(`Are you sure you want to ${user.isBanned ? 'unban' : 'ban'} ${user.name}?`)) {
      return;
    }

    const action = user.isBanned
      ? this.authService.unbanUser(user.id)
      : this.authService.banUser(user.id);

    action.subscribe({
      next: (updatedUser) => {
        // Update user in list
        this.users = this.users.map(u => u.id === updatedUser.id ? updatedUser : u);
      },
      error: (error) => {
        console.error('Error updating user status:', error);
        alert('Failed to update user status');
      }
    });
  }
}
