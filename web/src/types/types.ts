import {CreatedAt, PeriodicCreationUnit, EnumMessage} from '@xcan-angus/infra';
import {CombinedTargetType} from '@/enums/enums';

export type LoadingProps = {
  loading: boolean;
}

export type BasicProps = {
  projectId: string;
  projectName?: string;
  userInfo?: { id: string; };
  appInfo?: { id: string; };
  notify?: string;
  onShow?: boolean;
  refreshNotify?: number;
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
