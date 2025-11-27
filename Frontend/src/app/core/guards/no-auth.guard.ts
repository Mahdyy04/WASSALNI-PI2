import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthStore } from '../state/auth.store';

/**
 * No Auth Guard - protects routes that should NOT be accessible by authenticated users
 * Redirects to dashboard if user is already authenticated
 */
export const noAuthGuard: CanActivateFn = (route, state) => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    if (!authStore.isAuthenticated()) {
        return true;
    }

    // Redirect to appropriate dashboard based on role
    const user = authStore.currentUser();
    if (user?.role === 'DRIVER') {
        router.navigate(['/driver/dashboard']);
    } else if (user?.role === 'PASSENGER') {
        router.navigate(['/passenger/dashboard']);
    } else if (user?.role === 'ADMIN') {
        router.navigate(['/admin/dashboard']);
    } else {
        // Fallback
        router.navigate(['/']);
    }

    return false;
};
