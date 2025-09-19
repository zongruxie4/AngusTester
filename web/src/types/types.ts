import {CreatedAt, PeriodicCreationUnit, EnumMessage, User, AppInfo} from '@xcan-angus/infra';
import {CombinedTargetType} from '@/enums/enums';

export type LoadingProps = {
  loading: boolean;
}

export type BaseProps = {
  id: string;
  notify?: string;
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

export type ActivityItem = {
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
