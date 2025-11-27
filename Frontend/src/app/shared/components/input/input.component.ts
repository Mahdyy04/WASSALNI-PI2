import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

/**
 * Reusable input component with form integration
 * Implements ControlValueAccessor for reactive forms support
 */
@Component({
  selector: 'app-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true
    }
  ],
  template: `
    <div class="mb-4">
      @if (label) {
        <label [for]="id" class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
          {{ label }}
          @if (required) {
            <span class="text-red-500">*</span>
          }
        </label>
      }
      <input
        [id]="id"
        [type]="type"
        [placeholder]="placeholder"
        [disabled]="disabled"
        [class]="getInputClasses()"
        [value]="value"
        (input)="onInput($event)"
        (blur)="onBlur()"
      />
      @if (error) {
        <p class="mt-1 text-sm text-red-600 dark:text-red-400">{{ error }}</p>
      }
      @if (hint && !error) {
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">{{ hint }}</p>
      }
    </div>
  `,
  styles: []
})
export class InputComponent implements ControlValueAccessor {
  @Input() id = '';
  @Input() label = '';
  @Input() type: 'text' | 'email' | 'password' | 'number' | 'tel' | 'date' | 'time' = 'text';
  @Input() placeholder = '';
  @Input() disabled = false;
  @Input() required = false;
  @Input() error = '';
  @Input() hint = '';

  value = '';
  private onChange = (value: string) => {};
  private onTouched = () => {};

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value = target.value;
    this.onChange(this.value);
  }

  onBlur(): void {
    this.onTouched();
  }

  writeValue(value: string): void {
    this.value = value || '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  getInputClasses(): string {
    const baseClasses = 'input-field';
    const errorClasses = this.error 
      ? 'border-red-500 focus:ring-red-500' 
      : '';
    return `${baseClasses} ${errorClasses}`;
  }
}

