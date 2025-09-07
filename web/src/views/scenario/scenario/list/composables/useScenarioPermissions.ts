import { ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';
import { MenuItem, MenuItemPermission, ScenarioInfo } from '../types';

/**
 * Composable for managing scenario permissions and authorization
 */
export function useScenarioPermissions () {
  let actionTimer: NodeJS.Timeout;

  const permissionMap = ref<{
    [key: string]: {
      scenarioAuth: boolean;
      permissions: MenuItemPermission[];
    }
  }>({});

  /**
   * Handle mouse enter event for permission loading
   */
  const handleMouseEnter = async (data: ScenarioInfo) => {
    if (actionTimer) {
      clearTimeout(actionTimer);
    }

    // No need to load permissions for admins or items without auth
    if (appContext.isAdmin() || !data.auth) {
      return;
    }

    const id = data.id;
    const timestamp = permissionMap.value[id] ? 1000 : 300;
    actionTimer = setTimeout(async () => {
      const [error, { data: permissionData }] = await scenario.getCurrentScenarioAuth(id);
      if (error) {
        return;
      }

      permissionMap.value[id] = {
        ...permissionData,
        permissions: permissionData.permissions?.map(item => item.value)
      };
    }, timestamp);
  };

  /**
   * Handle mouse leave event
   */
  const handleMouseLeave = () => {
    if (actionTimer) {
      clearTimeout(actionTimer);
    }
  };

  /**
   * Check if a user has permission for a menu item
   */
  const hasPermission = (scenarioId: string, permission: MenuItemPermission): boolean => {
    // Admins have all permissions
    if (appContext.isAdmin()) {
      return true;
    }

    // Check if permission data exists for this scenario
    const permissions = permissionMap.value[scenarioId]?.permissions;
    if (!permissions) {
      return true; // Default to true if no permission data
    }

    return permissions.includes(permission);
  };

  /**
   * Filter dropdown menu items based on permissions and flags
   */
  const filterMenuItems = (
    menuItems: MenuItem[],
    scenarioInfo: ScenarioInfo,
    proTypeShowMap: { [key: string]: boolean }
  ): MenuItem[] => {
    return menuItems.filter(item => {
      const key = item.key;

      // Handle favorite/follow flag specific filtering
      if (key === 'cancelFavourite') {
        return scenarioInfo.favourite;
      }

      if (key === 'cancelFollow') {
        return scenarioInfo.follow;
      }

      if (key === 'favourite') {
        return !scenarioInfo.favourite;
      }

      if (key === 'follow') {
        return !scenarioInfo.follow;
      }

      // Handle task-related menu items based on project type settings
      return !(!proTypeShowMap.showTask && ['restartTestTask', 'createTestTask', 'reopenTestTask', 'deleteTestTask'].includes(key));
    });
  };

  return {
    // State
    permissionMap,

    // Methods
    handleMouseEnter,
    handleMouseLeave,
    hasPermission,
    filterMenuItems
  };
}
