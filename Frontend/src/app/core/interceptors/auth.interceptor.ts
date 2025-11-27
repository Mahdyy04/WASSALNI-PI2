import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthStore } from '../state/auth.store';

/**
 * HTTP Interceptor to add authentication token to requests
 * Automatically attaches the auth token from AuthStore to all HTTP requests
 * Uses AuthStore for authentication state
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authStore = inject(AuthStore);
  const token = authStore.getToken();

  // Clone request and add authorization header if token exists
  if (token) {
    const clonedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(clonedReq);
  }

  return next(req);
};

