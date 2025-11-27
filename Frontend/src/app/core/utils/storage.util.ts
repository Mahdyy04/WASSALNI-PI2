/**
 * Local storage utility functions
 */

/**
 * Get item from local storage
 */
export function getStorageItem<T>(key: string): T | null {
  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  } catch (error) {
    console.error(`Error getting item from storage: ${key}`, error);
    return null;
  }
}

/**
 * Set item in local storage
 */
export function setStorageItem<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.error(`Error setting item in storage: ${key}`, error);
  }
}

/**
 * Remove item from local storage
 */
export function removeStorageItem(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error(`Error removing item from storage: ${key}`, error);
  }
}

/**
 * Clear all local storage
 */
export function clearStorage(): void {
  try {
    localStorage.clear();
  } catch (error) {
    console.error('Error clearing storage', error);
  }
}

