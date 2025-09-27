import {AppInfo, CreatedAt, EnumMessage, PeriodicCreationUnit, User} from '@xcan-angus/infra';
import {CombinedTargetType} from '@/enums/enums';

export type LoadingProps = {
  loading: boolean;
}

export interface VisibleProps {
  visible: boolean;
}

export type BaseProps = {
  id: string;
  notify?: string;
}

export type DataSourceProps<T> = {
  dataSource: T;
}

export type BasicProps = {
  projectId: string;
  projectName?: string;
  userInfo?: { id: string; fullName?: string } | User;
  appInfo?: { id: string; } | AppInfo;
  notify?: string;
  onShow?: boolean;
  disabled?: false,
  refreshNotify?: string;
  data?: Record<string, string>;
}

export type UserInfo = {
  id: string;
  username: string;
  fullName: string;
  mobile: string;
  email: string;
  avatar: string;
}

// Time setting interface for CreatedDate component
export interface CreateTimeSetting {
  createdAt: CreatedAt;
  createdAtSomeDate?: string;
  periodicCreationUnit?: PeriodicCreationUnit;
  dayOfMonth?: string;
  dayOfWeek?: string;
  timeOfDay?: string;
  hourOfDay?: string;
}

export type ActivityInfo = {
  id: string;
  projectId: string;
  userId: string;
  fullName: string;
  avatar: string;
  targetId: string;
  parentTargetId: string;
  targetType: EnumMessage<CombinedTargetType>;
  targetName: string;
  optDate: string;
  description: string;
  detail: string;
  details?: string[];
}

export type AttachmentInfo = {
  name: string;
  url: string;
}

export type TagInfo = {
  id: string;
  name: string;
}

export type ProgressInfo = {
  total: number;
  completed: number;
  completedRate: number;
}

/**
 * Tree data structure interface - used for hierarchical data display
 * @interface TreeData
 */
export interface TreeData {
  /** Display name of the tree node */
  name: string;
  /** Sequence/order of the node */
  sequence: string;
  /** Depth level in the tree (0-based) */
  level: number;
  /** Child nodes array */
  children?: TreeData[];
  /** Unique identifier for the tree node */
  id: string;
  /** Parent node ID (if applicable) */
  pid?: string;
  /** Index position among siblings */
  index?: number;
  /** Array of parent IDs (path from root) */
  ids?: string[];
  /** Whether this is the last child in its level */
  isLast?: boolean;
  /** Maximum depth of child levels */
  childLevels?: number;
  /** Whether the node is disabled for interaction */
  disabled?: boolean;
}
