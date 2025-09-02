/**
 * Composable for managing tab navigation and state
 * Handles active tab changes and related logic
 */
import { ref } from 'vue';

/**
 * Composable for managing tab navigation and state
 * @returns Active tab state and change handler
 */
export function useTabs() {
  // Active tab state
  const activeTab = ref<number>(0);

  /**
   * Change active tab
   * @param num - Tab index to activate
   */
  const changeActiveTab = (num: number) => {
    activeTab.value = num;
  };

  return {
    activeTab,
    changeActiveTab
  };
}