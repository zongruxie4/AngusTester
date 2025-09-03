// Task information interface
export interface TaskInfo {
  id: string;
  name: string;
  code: string;
  taskType?: {
    value: string;
    message: string;
  };
  priority?: {
    value: string;
    message: string;
  };
  status?: {
    value: string;
    message: string;
  };
  assigneeName?: string;
  confirmorName?: string;
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
  status?: {
    value: 'NOT_RELEASED' | 'RELEASED' | 'ARCHIVED';
    message: string;
  };
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
  createdByName?: string;
  lastModifiedByName?: string;
  createdDate?: string;
  lastModifiedDate?: string;
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

// Component props interfaces
export interface VersionDetailProps {
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
  data: {
    _id: string;
    id: string | undefined;
  };
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

export interface SearchPanelProps {
  loading: boolean;
}

export interface IntroduceProps {
  showFunc: boolean;
}

// Browser tab pane interface
export interface IPane {
  _id: string;
  value: string;
  name?: string;
  closable?: boolean;
  noCache?: boolean;
  data?: {
    _id: string;
    id?: string;
  };
}

// Search and filter interfaces
export interface SearchFilters {
  key: string;
  op: string;
  value: string | string[];
}

export interface SearchParams {
  orderBy?: string;
  orderSort?: 'ASC' | 'DESC';
  filters: SearchFilters[];
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
export type OrderByKey = 'createdDate' | 'createdByName';
export type OrderSortKey = 'ASC' | 'DESC';
