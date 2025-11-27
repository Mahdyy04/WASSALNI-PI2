import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthStore } from '../../../core/state/auth.store';
import { UserRole } from '../../../core/models/user.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { InputComponent } from '../../../shared/components/input/input.component';
import { CardComponent } from '../../../shared/components/card/card.component';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    ButtonComponent,
    InputComponent,
    CardComponent
  ],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-md w-full">
        <div class="text-center mb-8">
          <h2 class="text-3xl font-bold text-gray-900 dark:text-gray-100">Welcome Back</h2>
          <p class="mt-2 text-gray-600 dark:text-gray-400">Sign in to your account</p>
        </div>

        <app-card>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <app-input
              id="email"
              label="Email"
              type="email"
              placeholder="your.email@example.com"
              formControlName="email"
              [required]="true"
              [error]="getErrorMessage('email')"
            />

            <app-input
              id="password"
              label="Password"
              type="password"
              placeholder="••••••••"
              formControlName="password"
              [required]="true"
              [error]="getErrorMessage('password')"
            />

            <div class="flex items-center justify-between mb-6">
              <div class="flex items-center">
                <input
                  id="remember-me"
                  type="checkbox"
                  formControlName="rememberMe"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <label for="remember-me" class="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                  Remember me
                </label>
              </div>
              <a routerLink="/auth/forgot-password" class="text-sm text-primary-600 hover:text-primary-500">
                Forgot password?
              </a>
            </div>

            <div *ngIf="errorMessage" class="mb-4 p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p class="text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
            </div>

            <app-button
              type="submit"
              variant="primary"
              size="lg"
              [loading]="loading"
              [fullWidth]="true"
            >
              Sign In
            </app-button>

            <div class="mt-6 text-center">
              <p class="text-sm text-gray-600 dark:text-gray-400">
                Don't have an account?
                <a routerLink="/auth/signup" class="font-medium text-primary-600 hover:text-primary-500">
                  Sign up
                </a>
              </p>
            </div>
          </form>
        </app-card>

       
  `,
  styles: []
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private authStore: AuthStore,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      const { email, password } = this.loginForm.value;

      this.authService.login(email, password).subscribe({
        next: (response: any) => {
          this.authStore.setAuthenticatedUser(response.user, response.token);
          this.loading = false;

          if (response.user.role === UserRole.ADMIN) {
            this.router.navigate(['/admin/dashboard']);
          } else if (response.user.role === UserRole.DRIVER) {
            this.router.navigate(['/driver/dashboard']);
          } else if (response.user.role === UserRole.PASSENGER) {
            this.router.navigate(['/passenger/dashboard']);
          } else {
            this.router.navigate(['/auth/role-selection']);
          }
        },
        error: (error: any) => {
          this.loading = false;
          this.errorMessage = error.message || 'Invalid email or password';
        }
      });
    }
  }

  getErrorMessage(controlName: string): string {
    const control = this.loginForm.get(controlName);
    if (control?.hasError('required') && control.touched) {
      return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} is required`;
    }
    if (control?.hasError('email') && control.touched) {
      return 'Please enter a valid email';
    }
    if (control?.hasError('minlength') && control.touched) {
      return 'Password must be at least 6 characters';
    }
    return '';
  }
}
