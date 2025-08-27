import { EnumMessage } from '@xcan-angus/infra';
import { ProjectType } from '@/enums/enums';

// Project type definition
export type Project = {
  id?: string;
  name?: string;
  description?: string;
  avatar?: string;
  ownerId?: string;
  ownerName?: string;
  type?: EnumMessage<ProjectType>;
  members?: {
    USER?: {name: string; avatar?: string}[];
    DEPT?: {name: string; avatar?: string}[];
    GROUP?: {name: string; avatar?: string}[];
  };
  membersNum?: number;
  showMembers?: {
    USER: {name: string; avatar?: string}[];
    DEPT: {name: string; avatar?: string}[];
    GROUP: {name: string; avatar?: string}[];
  };
  startDate?: string;
  deadlineDate?: string;
  createdBy?: string;
  lastModifiedDate?: string;
  // Temp fields
  dateRange?: [string, string];
  importExample?: boolean;
};

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

// Type definitions
export interface TreeData {
  name: string;
  sequence: string;
  level: number;
  children?: TreeData[];
  id: string;
  pid?: string;
  index?: number;
  ids?: string[];
  isLast?: boolean;
  childLevels?: number;
  disabled?: boolean;
}

// Type definitions
export type TagItem = {
  id: string;
  name: string;
  showName: string;
  showTitle: string;
  hasEditPermission: boolean;
}

export type TagProps = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  disabled: boolean;
}

// Type definitions
export interface AddTagProps {
  projectId: string;
  visible: boolean;
}
