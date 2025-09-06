// Monitor related types
export interface MonitorCount {
  avgDelayTime: string;
  failureNum: number;
  last7DayNum: string;
  last7DaySuccessNum: string;
  last7DaySuccessRate: string;
  last24HoursNum: string;
  last24HoursSuccessNum: string;
  last24HoursSuccessRate: string;
  last30DayNum: string;
  last30DaySuccessNum: string;
  last30DaySuccessRate: string;
  maxDelayTime: string;
  minDelayTime: string;
  p50DelayTime: string;
  p75DelayTime: string;
  p90DelayTime: string;
  successNum: number;
  successRate: string;
  totalNum: string;
}

export interface MonitorStatus {
  message: string;
  value: string;
}

export interface MonitorInfo {
  id: string;
  name: string;
  description?: string;
  status: MonitorStatus;
  count: MonitorCount;
  scenarioId?: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
}

// History execution record types
export interface HistoryRecord {
  createdBy: string;
  createdByName: string;
  createdDate: string;
  execEndDate: string;
  execId: string;
  execStartDate: string;
  failureMessage: string;
  id: string;
  monitorId: string;
  projectId: string;
  responseDelay: string;
  status: MonitorStatus;
}

export interface HistoryExecData {
  execId: string;
  execNode: string | {
    id: string;
    name: string;
    ip: string;
    agentPort: string;
    publicIp: string;
  };
  execStartDate: string;
  failureMessage?: string;
  sampleContents?: any[];
  sampleLogContent?: string;
  schedulingResult?: any;
  status: MonitorStatus;
}

// Component props types
export interface MonitorDetailProps {
  projectId: string;
  userInfo: { id: string };
  appInfo: { id: string };
  data: {
    _id: string;
    id: string | undefined;
  };
}

// Chart component props
export interface ChartProps {
  count?: {
    failureNum: number | string;
    successNum: number | string;
    successRate: string;
  };
}

// Tab pane injection type
export type AddTabPaneFunction = (params: {
  value: string;
  _id: string;
  id: string;
  data: any;
  name: string;
}) => void;

// Status color configuration
export interface StatusColorConfig {
  SUCCESS: string;
  PENDING: string;
  FAILURE: string;
}
