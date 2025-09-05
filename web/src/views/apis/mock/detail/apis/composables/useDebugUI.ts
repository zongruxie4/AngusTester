import { ref } from 'vue';

/**
 * Composable for debug UI state management
 * Handles UI state like spread/collapse and debug mode toggle
 */
export function useDebugUI () {
  // UI state
  const spread = ref(false);
  const showDebug = ref(true);

  /**
   * Toggle spread/collapse state
   */
  const toggleSpread = () => {
    spread.value = !spread.value;
  };

  /**
   * Change debug mode visibility
   * @param value - New debug mode state
   */
  const changeShowDebug = (value: boolean) => {
    showDebug.value = value;
  };

  return {
    // State
    spread,
    showDebug,

    // Methods
    toggleSpread,
    changeShowDebug
  };
}
