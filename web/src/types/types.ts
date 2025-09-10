import {CreatedAt, PeriodicCreationUnit} from '@xcan-angus/infra';

export type BasicProps = {
  projectId: string;
  userInfo?: { id: string; };
  appInfo?: { id: string; };
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
