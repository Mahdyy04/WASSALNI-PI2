/**
 * Date utility functions
 */

/**
 * Format date to readable string
 */
export function formatDate(date: Date): string {
  return date.toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

/**
 * Format time to HH:MM
 */
export function formatTime(time: string): string {
  return time;
}

/**
 * Format datetime to readable string
 */
export function formatDateTime(date: Date, time: string): string {
  return `${formatDate(date)} à ${formatTime(time)}`;
}

/**
 * Check if date is today
 */
export function isToday(date: Date): boolean {
  const today = new Date();
  return date.toDateString() === today.toDateString();
}

/**
 * Check if date is tomorrow
 */
export function isTomorrow(date: Date): boolean {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return date.toDateString() === tomorrow.toDateString();
}

