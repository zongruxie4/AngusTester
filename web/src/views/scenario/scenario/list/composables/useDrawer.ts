import { computed } from 'vue';

/**
 * Composable for managing drawer functionality
 * @param t - Translation function from useI18n
 */
export function useDrawer (t: (key: string) => string) {
  /**
   * Menu items for the drawer
   */
  const menuItems = computed(() => [
    {
      icon: 'icon-lishijilu',
      name: t('common.activity'),
      key: 'activity'
    }
  ]);

  /**
   * Activity types
   */
  const types = ['SCENARIO' as const];

  return {
    // Configurations
    menuItems,
    types
  };
}
