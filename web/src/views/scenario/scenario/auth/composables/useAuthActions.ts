import { scenario } from '@/api/tester';
import type { PermissionMapItem } from '../types';
import { AuthObjectType as AuthObjectTypeEnum } from '@xcan-angus/infra';

/**
 * Composable for handling authentication actions (enable/disable, permission changes)
 */
export function useAuthActions (
  dataMap: { [key: string]: any },
  permissionsMap: { [key: string]: PermissionMapItem | undefined },
  enabledLoadingMap: { [key: string]: boolean },
  updatingMap: { [key: string]: boolean },
  permissionValues: string[],
  authObjectId: string,
  authObjectType: AuthObjectTypeEnum
) {
  /**
   * Handle switch change for enabling/disabling scenario auth
   */
  const handleSwitchChange = async (checked: boolean, id: string) => {
    enabledLoadingMap[id] = true;
    const [error] = await scenario.enableScenarioAuth(id, checked);
    enabledLoadingMap[id] = false;

    if (error) {
      return;
    }

    dataMap[id].auth = checked;
  };

  /**
   * Handle check all change for permissions
   */
  const handleCheckAllChange = async (event: { target: { checked: boolean } }, id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const checked = event.target.checked;
    const permissions = checked ? permissionValues : [];
    await handlePermissionChange(permissions, id);
  };

  /**
   * Handle individual permission changes
   */
  const handlePermissionChange = async (permissions: string[], id: string) => {
    if (updatingMap[id]) {
      return;
    }

    const authId = permissionsMap[id]?.id;

    // If no permissions selected, delete auth
    if (!permissions.length) {
      updatingMap[id] = true;
      const [error] = await scenario.deleteScenarioAuth(authId!);
      updatingMap[id] = false;

      if (error) {
        return;
      }

      permissionsMap[id] = undefined;
      return;
    }

    // If auth exists, update it
    if (authId) {
      updatingMap[id] = true;
      const [error] = await scenario.putScenarioAuth(authId, { permissions });
      updatingMap[id] = false;

      if (error) {
        return;
      }

      permissionsMap[id]!.permissions = permissions;
      return;
    }

    // Create new auth
    updatingMap[id] = true;
    const params = {
      permissions,
      authObjectId,
      authObjectType
    };
    const [error, { data = { id: '' } }] = await scenario.addScenarioAuth(id, params);
    updatingMap[id] = false;

    if (error) {
      return;
    }

    permissionsMap[id] = {
      id: data.id,
      creatorFlag: false,
      permissions: permissions
    };
  };

  return {
    handleSwitchChange,
    handleCheckAllChange,
    handlePermissionChange
  };
}
