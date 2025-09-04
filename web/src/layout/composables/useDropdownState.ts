import { ref } from 'vue';
import type { DropdownState } from '../types';

/**
 * Composable for managing dropdown visibility state
 */
export function useDropdownState () {
  const dropdownState = ref<DropdownState>({
    visible: false
  });

  let timer: NodeJS.Timeout;

  /**
   * Show dropdown with delay
   */
  const showDropdown = (delay = 300): void => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      dropdownState.value.visible = true;
    }, delay);
  };

  /**
   * Hide dropdown with delay
   */
  const hideDropdown = (delay = 300): void => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      dropdownState.value.visible = false;
    }, delay);
  };

  /**
   * Toggle dropdown visibility
   */
  const toggleDropdown = (): void => {
    clearTimeout(timer);
    dropdownState.value.visible = !dropdownState.value.visible;
  };

  /**
   * Handle mouse enter event
   */
  const handleMouseEnter = (): void => {
    clearTimeout(timer);
    // Clear any pending hide timer and show immediately
    dropdownState.value.visible = true;
  };

  /**
   * Handle mouse leave event
   */
  const handleMouseLeave = (): void => {
    // Add delay when leaving to prevent flickering
    // This allows user to move mouse from trigger to dropdown content
    hideDropdown(150);
  };

  /**
   * Handle project selection with quick hide
   */
  const handleProjectSelection = (): void => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      dropdownState.value.visible = false;
    }, 100);
  };

  /**
   * Cleanup timer on unmount
   */
  const cleanup = (): void => {
    clearTimeout(timer);
  };

  return {
    dropdownState,
    showDropdown,
    hideDropdown,
    toggleDropdown,
    handleMouseEnter,
    handleMouseLeave,
    handleProjectSelection,
    cleanup
  };
}
