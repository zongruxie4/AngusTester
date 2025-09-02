import { computed } from 'vue';
import { script } from '@/api/tester';

/**
 * <p>
 * Composable for managing authentication actions including enabling/disabling
 * script permissions and updating permission sets.
 * </p>
 * <p>
 * Handles all CRUD operations for script authentication and permission management.
 * </p>
 */
export function useAuthActions(
  dataMap: any,
  permissionsMap: any,
  enabledLoadingMap: any,
  updatingMap: any,
  authObjectId: string | undefined,
  type: 'user' | 'dept' | 'group',
  permissions: { value: string; label: string }[]
) {
  /**
   * <p>
   * Computed property that returns all permission values from the permissions array.
   * Used for checking all permissions at once.
   * </p>
   */
  const permissionValues = computed(() => {
    return permissions.map(item => item.value);
  });

  /**
   * <p>
   * Toggles the authentication status for a specific script by enabling or disabling it.
   * Updates the local state and makes API call to persist changes.
   * </p>
   */
  const switchChange = async (checked: boolean, id: string) => {
    enabledLoadingMap.value[id] = true;
    const [error] = await script.enableScriptAuth(id, checked);
    enabledLoadingMap.value[id] = false;
    
    if (error) {
      return;
    }

    if (checked) {
      dataMap.value[id].auth = true;
      return;
    }

    dataMap.value[id].auth = false;
  };

  /**
   * <p>
   * Handles the "check all" checkbox change event for a script.
   * Selects or deselects all permissions for the given script.
   * </p>
   */
  const checkAllChange = async (event: { target: { checked: boolean } }, id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const checked = event.target.checked;
    const permissions: string[] = checked ? permissionValues.value : [];

    checkChange(permissions, id);
  };

  /**
   * <p>
   * Core function for updating permissions for a specific script.
   * Handles three scenarios: removing all permissions, updating existing permissions,
   * or creating new permissions.
   * </p>
   */
  const checkChange = async (permissions: string[], id: string) => {
    if (updatingMap[id]) {
      return;
    }

    // If permissionMap.value[id].id is undefined, it means not authorized, otherwise already authorized
    const authId = permissionsMap.value[id]?.id;
    
    if (!permissions.length) {
      // Remove all permissions
      updatingMap[id] = true;
      const [error] = await script.deleteScriptAuth(authId!);
      updatingMap[id] = false;
      
      if (error) {
        return;
      }

      permissionsMap.value[id] = undefined;
      return;
    }

    if (authId) {
      // Update existing permissions
      updatingMap[id] = true;
      const [error] = await script.putScriptAuth(authId, { permissions });
      updatingMap[id] = false;
      
      if (error) {
        return;
      }

      permissionsMap.value[id]!.permissions = permissions;
      return;
    }

    // Create new permissions (no previous authorization)
    updatingMap[id] = true;
    const params = { 
      permissions, 
      authObjectId: authObjectId!, 
      authObjectType: type 
    };
    const [error, { data = { id: '' } }] = await script.addScriptAuth(id, params);
    updatingMap[id] = false;
    
    if (error) {
      return;
    }

    permissionsMap.value[id] = {
      id: data.id,
      creatorFlag: false,
      permissions: permissions
    };
  };

  return {
    permissionValues,
    switchChange,
    checkAllChange,
    checkChange
  };
}
