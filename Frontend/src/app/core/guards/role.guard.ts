import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthStore } from '../state/auth.store';
import { UserRole } from '../models/user.model';

/**
 * Role Guard - protects routes based on user role
 * Checks if user has the required role
 * Uses AuthStore for authentication state
 */
export const roleGuard = (allowedRoles: UserRole[]): CanActivateFn => {
  return (route, state) => {
    const authStore = inject(AuthStore);
    const router = inject(Router);
    const currentUser = authStore.currentUser();

    if (currentUser && allowedRoles.includes(currentUser.role)) {
      return true;
    }

    router.navigate(['/']);
    return false;
  };
};

