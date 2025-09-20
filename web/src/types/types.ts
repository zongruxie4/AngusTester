import {CreatedAt, PeriodicCreationUnit, EnumMessage, User, AppInfo} from '@xcan-angus/infra';
import {CombinedTargetType} from '@/enums/enums';

export type LoadingProps = {
  loading: boolean;
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
