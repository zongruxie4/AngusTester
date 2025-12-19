import { EnumMessage, Priority } from '@xcan-angus/infra';
import { TaskType, TaskStatus, SoftwareVersionStatus } from '@/enums/enums';

// Task information interface
export interface TaskInfo {
  id: string;
  name: string;
  code: string;
  taskType?: EnumMessage<TaskType>;
  priority?: EnumMessage<Priority>;
  status?: EnumMessage<TaskStatus>;
  assigneeName?: string;
  confirmerName?: string;
  deadlineDate?: string;
  sprintName?: string;
  overdue?: boolean;
  linkUrl?: string;
}

// Base version information interface
export interface VersionInfo {
  id: string;
  name: string;
  description?: string;
  startDate?: string;
  releaseDate?: string;
  status?: EnumMessage<SoftwareVersionStatus>;
  progress?: {
    completedNum: number;
    completedRate: number;
    completedWorkload: number;
    evalWorkload: number;
    totalNum: number;
  };
  taskByStatus?: {
    [key: string]: TaskInfo[];
  };
  creator?: string;
  modifier?: string;
  createdDate?: string;
  modifiedDate?: string;
}

// Chart data interfaces
export interface ChartValue {
  title: string;
  value: Array<{
    name?: string;
    value: string | number;
  }>;
}

export interface ChartProps {
  chart1Value: ChartValue;
  chart2Value: ChartValue;
}

export interface VersionListProps {
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
  notify: string;
  showDetail: boolean;
}

export interface VersionEditProps {
  visible: boolean;
  versionId?: string;
  projectId: string;
}

export interface VersionMergeProps {
  visible: boolean;
  projectId: string;
}

export interface MyTaskProps {
  projectId: string;
  userInfo: { id: string };
  notify: string;
  versionId: string;
  versionInfo: VersionInfo;
}

export interface MyTaskTableProps {
  taskList: TaskInfo[];
}

export interface IntroduceProps {
  showFunc: boolean;
}

// Pagination interface
export interface PaginationConfig {
  current: number;
  pageSize: number;
  total: number;
}

// Form state interfaces
export interface VersionFormState {
  name?: string;
  startDate?: string;
  releaseDate?: string;
  description?: string;
}

export interface MergeFormState {
  formId?: string;
  toId?: string;
}

// Menu item interface
export interface MenuItem {
  key: string;
  name: string;
  icon?: string;
}

// Status color configuration
export interface StatusColorConfig {
  [key: string]: string;
}

// Order configuration
export type OrderByKey = 'createdDate' | 'creator';
