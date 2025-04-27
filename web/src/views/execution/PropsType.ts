export type ExecuteListObj = {
  canUpdateTestResult: boolean;
  id: string;
  no: string;
  name: string;
  lastExecId: string;
  plugin: string;
  scriptType: {
      value: string;
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
  startMode: string;
  startAtDate: string;
  reportInterval: string;
  timeout: string;
  actualStartDate: string;
  endDate: string;
  createdBy: string;
  createdByName: string;
  scriptName: string;
  createdDate: string;
  editname: boolean;
  editpriority: boolean;
  editThread: boolean;
  trial: boolean;
  editstartDate: boolean;
  sampleSummary: string;
  updateTestResult: boolean;
  editduration: boolean;
  currentDuration: string;
  durationProgress: string;
  iterationsProgress: string;
  currentIterations: string;
  currentDurationProgress: string;
  editreportInterval: boolean;
  edititerations: boolean;
  currentIterationsProgress: string;
  sampleSummaryInfo: any;
  hasOperationPermission:boolean;
  actionPermission:string[];
  lastSchedulingResult:any[];
  meterMessage: string;
  meterStatus: string;
  startStopMessage: string;
  startStopExitCode: string;
  ignoreAssertions: boolean;
  errMessage?:{codeName:string;messageName:string;code:string;message:string;}
}

export type SearchKey = 'all' | 'createdBy' | 'execBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek'
