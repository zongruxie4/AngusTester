export type GroupedKey = 'createdBy' | 'plugin' | 'scriptType' |'none';

export type SceneInfo = {
    description:string;
    id: string;
    name: string;
    dirId: string;
    dirName: string;
    plugin: 'WebSocket'|'Jdbc'|'Http';
    authFlag: boolean;
    createdBy: string;
    createdByName: string;
    avatar: string;
    createdDate: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    favouriteFlag: boolean;
    followFlag: boolean;
    scriptType:{
      message:string;
      value:string;
    };
    scriptId:string;
}

export type MenuItemKey =
  'edit' |
  'clone' |
  'delete' |
  'performTesting' |
  'followFlag' |
  'cancelFollowFlag' |
  'favouriteFlag' |
  'cancelFavouriteFlag' |
  'auth' |
  'export' |
  'move' |
  'restartTestTask'|
  'createTestTask' |
  'reopenTestTask' |
  'deleteTestTask'

export type MenuItemPermission = 'MODIFY' | 'EXECUTE' | 'VIEW' | 'GRANT'|'TEST'|'EXPORT'|'DELETE';

export type MenuItem = {
  key: MenuItemKey;
  name: string;
  icon: string;
  permission: MenuItemPermission;
  tip?: string;
}
