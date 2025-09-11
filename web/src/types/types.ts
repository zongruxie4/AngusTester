import {CreatedAt, PeriodicCreationUnit} from '@xcan-angus/infra';

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
