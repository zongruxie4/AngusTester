import { ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';

/**
 * Composable for managing search functionality
 * @returns Object containing search state and methods
 */
export function useSearch () {
  const inputValue = ref<string>('');

  /**
   * Debounced input change handler to optimize performance
   * @param callback - Callback function to execute on search
   */
  const createInputChangeHandler = (callback?: (value: string) => void) => {
    return debounce(duration.search, (event: Event) => {
      const target = event.target as HTMLInputElement;
      inputValue.value = target.value;
      callback?.(target.value);
    });
  };

  /**
   * Clear search input
   */
  const clearSearch = () => {
    inputValue.value = '';
  };

  /**
   * Set search value programmatically
   * @param value - Search value to set
   */
  const setSearchValue = (value: string) => {
    inputValue.value = value;
  };

  return {
    inputValue,
    createInputChangeHandler,
    clearSearch,
    setSearchValue
  };
}
