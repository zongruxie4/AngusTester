import { computed, ComputedRef } from 'vue';
import { appContext } from '@xcan-angus/infra';
import type { ProjectTypeVisibility } from '../types';

/**
 * Composable for managing menu configuration
 */
export function useMenuConfiguration (projectTypeVisibility: ComputedRef<ProjectTypeVisibility>) {
  /**
   * Get header menus based on project type
   */
  const headerMenus = computed(() => {
    const menuList = appContext.getAccessAppFuncTree();
    const excludedMenus = ['Projects', 'Config'];

    // Add Task to excluded menus if not visible
    if (!projectTypeVisibility.value.showTask) {
      excludedMenus.push('Task');
    }

    return menuList?.filter(item => !excludedMenus.includes(item.code)) || [];
  });

  /**
   * Get project menus
   */
  const projectMenus = computed(() => {
    const menuList = appContext.getAccessAppFuncTree();
    return menuList?.filter(item => ['Projects', 'Config'].includes(item.code)) || [];
  });

  /**
   * Get application function code map
   */
  const appFunctionCodeMap = computed(() => {
    return appContext.getAccessAppFuncCodeMap();
  });

  return {
    headerMenus,
    projectMenus,
    appFunctionCodeMap
  };
}
