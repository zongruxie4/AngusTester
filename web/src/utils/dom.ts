import { debounce } from 'throttle-debounce';
import { localStore } from '@xcan-angus/infra';

// ----------------------------------------------------
// Scroll position management utilities
// ----------------------------------------------------

/**
 * Scroll position manager that persists scroll state to local storage.
 * <p>
 * Automatically restores scroll position on initialization and
 * saves scroll position changes with debounced updates.
 * </p>
 */
class Scroll {
    private element: HTMLElement;
    private storageKey: string;
    private isListenerActive: boolean;

    /**
     * Initialize scroll manager for a specific element.
     * @param element - Target HTML element to manage scroll for
     * @param storageKey - Key for storing scroll position in local storage
     */
    constructor (element: HTMLElement, storageKey: string) {
      this.element = element;
      this.storageKey = storageKey;
      this.isListenerActive = false;
    }

    /**
     * Initialize scroll position management.
     * <p>
     * Restores cached scroll position and starts listening for scroll events.
     * Uses a 2-second delay for position restoration to ensure DOM is ready.
     * </p>
     */
    bootstrap (): void {
      const cachedScrollTop = localStore.get(this.storageKey);
      if (cachedScrollTop) {
        setTimeout(() => {
          this.element.scrollTo(0, +cachedScrollTop);
        }, 2000);
      }
      // Start listening for scroll events if not already active
      if (this.isListenerActive) {
        return;
      }
      this.addScrollListener();
    }

    /**
     * Handle scroll events and save position to storage.
     * @param event - Scroll event
     */
    private handleScroll (event: Event): void {
      const scrollTop = (event.target as HTMLElement).scrollTop + '';
      localStore.set(this.storageKey, scrollTop);
    }

    /**
     * Add debounced scroll event listener.
     * <p>
     * Uses 50ms debounce to prevent excessive storage writes.
     * </p>
     */
    private addScrollListener (): void {
      this.element.addEventListener('scroll', debounce(50, (e) => { this.handleScroll(e); }));
      this.isListenerActive = true;
    }

    /**
     * Remove scroll event listener and reset state.
     */
    remove (): void {
      this.element.removeEventListener('scroll', this.handleScroll);
      this.isListenerActive = false;
    }
}

export { Scroll };
