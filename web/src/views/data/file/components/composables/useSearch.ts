import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

/**
 * Composable for managing file search functionality
 * Handles search visibility, parameters, and search execution
 *
 * @returns Object containing reactive state and methods for search operations
 */
export function useSearch () {
  // Reactive state
  const isSearchVisible = ref(false);
  const spaceId = ref<string>();
  const inputValue = ref<string>('');

  // Route instance for accessing route parameters
  const route = useRoute();

  /**
   * Show the search input
   */
  const showSearch = (): void => {
    isSearchVisible.value = true;
  };

  /**
   * Computed parameters for search API calls
   */
  const searchParams = computed(() => {
    return {
      spaceId: spaceId.value
    };
  });

  /**
   * Handle search input change
   *
   * @param value - Selected value
   */
  const handleSearchChange = (value: string): void => {
    if (!value) {
      inputValue.value = '';
      return;
    }

    inputValue.value = value;
  };

  /**
   * Handle dropdown visibility change
   *
   * @param visible - Dropdown visibility state
   */
  const handleDropdownVisibility = (visible: boolean): void => {
    if (!visible && !inputValue.value) {
      isSearchVisible.value = false;
    }
  };

  /**
   * Initialize composable - set space ID from route on mount
   */
  const init = (): void => {
    onMounted(() => {
      if (route.params.id) {
        spaceId.value = route.params.id as string;
      }
    });
  };

  return {
    // State
    isSearchVisible,
    spaceId,
    inputValue,
    searchParams,

    // Methods
    showSearch,
    handleSearchChange,
    handleDropdownVisibility,
    init
  };
}
