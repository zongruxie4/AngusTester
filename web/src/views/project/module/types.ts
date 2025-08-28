// Type definitions
export type ModuleItem = {
  id: string;
  name: string;
  key?: string;
  showName?: string;
  showTitle?: string;
  children?: ModuleItem[];
  level?: number;
  index?: number;
  ids?: string[];
  isLast?: boolean;
  pid?: string;
  sequence?: string;
  childLevels?: number;
  hasEditPermission?: boolean;
}

export type ModuleProps = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  disabled: boolean;
  projectName: string;
}

// Type definitions
export interface AddModuleProps {
  projectId: string;
  visible: boolean;
  pid?: string;
}

// Type definitions
export interface MoveModuleProps {
  visible: boolean;
  projectId: string;
  projectName: string;
  module: {
    id: string;
    childLevels: number;
    pid: string;
  }
}
