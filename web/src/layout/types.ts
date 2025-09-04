import { EnumMessage } from '@xcan-angus/infra';
import { ProjectType } from '@/enums/enums';

/**
 * Project information interface
 */
export interface ProjectInfo {
  id: string;
  avatar: string;
  name: string;
  type: EnumMessage<ProjectType>;
  ownerId: string;
  description: string;
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

/**
 * Simplified project info for display
 */
export interface ProjectDisplayInfo {
  id: string;
  avatar: string;
  name: string;
  createdBy: string;
  ownerId: string;
  type: EnumMessage<ProjectType>;
}

/**
 * Project type visibility configuration
 */
export interface ProjectTypeVisibility {
  showTask: boolean;
  showBackLog: boolean;
  showSprint: boolean;
  showMeeting: boolean;
  showTaskStatistics: boolean;
}

/**
 * User information
 */
export interface UserInfo {
  id: string;
}

/**
 * Menu item interface
 */
export interface MenuItem {
  code: string;
  name: string;
  url?: string;
  hasAuth?: boolean;
  showName?: string;
}

/**
 * Dropdown visibility state
 */
export interface DropdownState {
  visible: boolean;
  timer?: NodeJS.Timeout;
}

/**
 * Project search state
 */
export interface ProjectSearchState {
  query: string;
  filteredList: ProjectInfo[];
}
