import { EnumMessage, ScriptType, ScriptSource, StartMode } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';

export type MenuItem = {
  key: 'none' | 'createdBy' | 'lastModifiedBy' | 'execBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek';
  name: string;
}

export interface NodeItem {
  agentPort: string; domain: string; id: string; ip: string; name: string; publicIp:string;
}

export interface Exception {
  code: string,
  message: string,
  codeName: string,
  messageName: string
}

export type ExecutionInfo = {
  id: string;
  no: string;
  name: string;
  plugin: string;
  scriptType: EnumMessage<ScriptType>,
  scriptId: string;
  scriptName: string;
  scriptSource: EnumMessage<ScriptSource>
  status: EnumMessage<ExecStatus>,
  startMode: EnumMessage<StartMode>,
  iterations: string;
  duration: string;
  thread: string;
  priority: string;
  startAtDate: string;
  reportInterval: string;
  timeout: string;
  actualStartDate: string;
  endDate: string;
  lastExecId: string;
  canUpdateTestResult: boolean;
  trial: boolean;
  sampleSummary: string;
  updateTestResult: boolean;
  batchRows?:string;

  meterMessage: string;
  meterStatus: string;
  ignoreAssertions: boolean;

  configuration: {
    thread: {
      threads: string;
    },
    priority: string;
    reportInterval: string;
  },
  task: Record<string, any>;
  pipelineTargetMappings?:Record<string, any>;

  createdBy: string;
  createdByName:string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName:string;
  lastModifiedDate: string;

  currentDuration: string;
  durationProgress: string;
  iterationsProgress: string;
  currentIterations: string;
  currentDurationProgress: string;
  currentIterationsProgress: string;
  sampleSummaryInfo:Record<string, any>;
  hasOperationPermission: boolean;
  lastSchedulingResult?:any[];
  schedulingNum?: string;
  lastSchedulingDate?: string;
  actionPermission: string[];

  appNodes: NodeItem[];
  execNodes: NodeItem[];

  startStopMessage: string;
  startStopExitCode: string;

  // Temp fields in web
  editThread: boolean;
  editName: boolean;
  editPriority: boolean;
  editStartDate: boolean;
  editDuration: boolean;
  editReportInterval: boolean;
  editIterations: boolean;
  errMessage?: Exception;
}

// Chart data interfaces
export interface ChartDataItem {
  name: string;
  value: number;
  codes?: number;
}

// Pie chart component props
export interface PieChartProps {
  color: string[];
  type: string;
  title: string;
  total: number;
  dataSource: ChartDataItem[];
}

// Search panel interfaces
export interface SearchPanelProps {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

export interface FilterItem {
  key: string;
  op: string;
  value: boolean | string | string[];
}

// UI configuration interfaces
export interface ScriptTypeConfig {
  text: string;
  color: string;
}

export interface StatusColorMap {
  [key: string]: string;
}

export interface DropdownMenuItem {
  key: string;
  icon: string;
  name: string;
  permission?: string;
  noAuth?: boolean;
}
