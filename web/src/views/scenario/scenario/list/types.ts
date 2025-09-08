/**
 * Grouping keys for scenarios
 */
export type GroupedKey = 'createdBy' | 'plugin' | 'scriptType' | 'none';

/**
 * Script type structure
 */
export interface ScriptType {
  message: string;
  value: string;
}

/**
 * Scenario information structure
 */
export interface ScenarioInfo {
  description: string;
  id: string;
  name: string;
  dirId: string;
  dirName: string;
  plugin: 'WebSocket' | 'Jdbc' | 'Http';
  auth: boolean;
  createdBy: string;
  createdByName: string;
  avatar: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  favourite: boolean;
  follow: boolean;
  scriptType: ScriptType;
  scriptId: string;
  lastExecStatus?: {
    message: string;
    value: string;
  };
  lastExecFailureMessage?: string;
  nameLinkUrl?: string;
  detailLink?: string;
}

/**
 * Menu item keys
 */
export type MenuItemKey =
  'edit' |
  'clone' |
  'delete' |
  'performTesting' |
  'follow' |
  'cancelFollow' |
  'favourite' |
  'cancelFavourite' |
  'auth' |
  'export' |
  'move' |
  'restartTestTask' |
  'createTestTask' |
  'reopenTestTask' |
  'deleteTestTask';

/**
 * Menu item permissions
 */
export type MenuItemPermission = 'MODIFY' | 'EXECUTE' | 'VIEW' | 'GRANT' | 'TEST' | 'EXPORT' | 'DELETE';

/**
 * Menu item structure
 */
export interface MenuItem {
  key: MenuItemKey;
  name: string;
  icon: string;
  permission: MenuItemPermission;
  tip?: string;
}
