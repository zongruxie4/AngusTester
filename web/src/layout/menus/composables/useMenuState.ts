import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { AuthAppFuncTree } from '@xcan-angus/infra';

/**
 * Composable for managing menu state and selection
 */
export function useMenuState (menus: AuthAppFuncTree[]) {
  const route = useRoute();

  // State
  const menuList = ref<AuthAppFuncTree[]>([]);
  const selectedUrl = ref(location.pathname);

  /**
   * Handle menu selection
   */
  const handleMenuSelect = (menu: AuthAppFuncTree): void => {
    selectedUrl.value = menu.url || '';
  };

  /**
   * Check if a menu item is active based on current URL
   */
  const isMenuActive = (url: string): boolean => {
    if (!url) {
      return false;
    }
    return !!(selectedUrl.value && (url.startsWith(selectedUrl.value) || selectedUrl.value.startsWith(url)));
  };

  /**
   * Find current menu based on route
   */
  const findCurrentMenu = (): AuthAppFuncTree | undefined => {
    return menuList.value.find(item => {
      return selectedUrl.value && item.url && (
        item.url.startsWith(selectedUrl.value) ||
        selectedUrl.value.startsWith(item.url)
      );
    });
  };

  /**
   * Initialize menu list
   */
  const initializeMenuList = (newMenus: AuthAppFuncTree[]): void => {
    if (!newMenus?.length) {
      return;
    }

    menuList.value = newMenus;

    const currentMenu = findCurrentMenu();
    if (currentMenu) {
      handleMenuSelect(currentMenu);
    }
  };

  /**
   * Watch for route changes
   */
  const watchRouteChanges = (): void => {
    watch(() => route?.path, (newValue) => {
      selectedUrl.value = newValue;
    }, { immediate: true });
  };

  /**
   * Watch for menu prop changes
   */
  const watchMenuChanges = (): void => {
    watch(() => menus, (newValue) => {
      initializeMenuList(newValue);
    }, { immediate: true });
  };

  return {
    // State
    menuList,
    selectedUrl,

    // Methods
    handleMenuSelect,
    isMenuActive,
    findCurrentMenu,
    initializeMenuList,
    watchRouteChanges,
    watchMenuChanges
  };
}
