export interface NodeItem {
  agentPort: string; domain: string; id: string; ip: string; name: string; publicIp:string;
}
export type ExecObj = {
  id: string;
  name: string;
  plugin: string;
  appNodes: NodeItem[];
  execNodes: NodeItem[];
  scriptType: {
      value: 'TEST_FUNCTIONALITY'|'TEST_PERFORMANCE'|'TEST_STABILITY'|'TEST_CUSTOMIZATION'|'MOCK_DATA'|'MOCK_APIS';
      message: string;
  },
  scriptId: string;
  status: {
      value: string;
      message: string;
  },
  iterations: string;
  duration: string;
  thread: string;
  priority: string;
  startMode: {
      value: string;
      message: string;
  },
  reportInterval: string;
  configuration: {
      thread: {
          threads: string;
      },
      priority: string;
      reportInterval: string;
  },
  task: Record<string, any>;
  actualStartDate: string;
  endDate: string;
  createdBy: string;
  creator:string;
  createdDate: string;
  modifiedBy: string;
  modifier:string;
  modifiedDate: string;
  currentDuration: string;
  currentDurationProgress: string;
  iterationsProgress: boolean;
  currentIterations: string;
  currentIterationsProgress: string;
  durationProgress: boolean;
  hasOperationPermission: boolean;
  startStopMessage: string;
  startStopExitCode: string;
  sampleSummaryInfo:Record<string, any>;
  pipelineTargetMappings?:Record<string, any>;
  lastSchedulingResult?:any[];
  meterStatus?:string;
  meterMessage?:string;
  batchRows?:string;
}
export interface Exception {code: string, message: string, codeName: string, messageName: string}
