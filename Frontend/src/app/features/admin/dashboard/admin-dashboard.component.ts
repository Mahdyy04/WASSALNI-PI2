import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarComponent, SidebarItem } from '../../../shared/components/sidebar/sidebar.component';

/**
 * Admin Dashboard component
 * Professional dashboard with sidebar navigation
 * Tabs for: All Rides History, Users Management, Reports Management, Ban Users
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet, SidebarComponent],
  template: `
    <div class="flex min-h-screen bg-gray-50 dark:bg-gray-900">
      <app-sidebar [title]="'Admin Panel'" [items]="sidebarItems"></app-sidebar>
      
      <div class="flex-1 p-8">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: []
})
export class AdminDashboardComponent {
  sidebarItems: SidebarItem[] = [
    { label: 'Dashboard Overview', route: '/admin/dashboard/overview', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6' },
    { label: 'All Rides', route: '/admin/dashboard/rides', icon: 'M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z' },
    { label: 'Users Management', route: '/admin/dashboard/users', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z' },
    { label: 'Reports Management', route: '/admin/dashboard/reports', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z' },
    { label: 'Ban Users', route: '/admin/dashboard/ban-users', icon: 'M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636' }
  ];

  constructor(public authService: AuthService) {}
}

