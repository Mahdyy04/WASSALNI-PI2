import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthStore } from '../state/auth.store';

/**
 * Admin Guard - protects routes that require admin role
 * Redirects to home if user is not an admin
 * Uses AuthStore for authentication state
 */
export const adminGuard: CanActivateFn = (route, state) => {
  const authStore = inject(AuthStore);
  const router = inject(Router);

  if (authStore.isAdmin()) {
    return true;
  }

  router.navigate(['/']);
  return false;
};

