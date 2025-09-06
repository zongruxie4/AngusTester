import { EnumMessage, ScriptType, StartMode } from '@xcan-angus/infra';
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

export interface PieSetting {
  key: string;
  value: string;
  type: { message: string; value: string; }[]
}

export interface PieData {
  key: string;
  title: string;
  total: number;
  color: string [];
  legend:{value:string, message:string}[];
  data: { name: string, value: number, codes?:number }[];
}
