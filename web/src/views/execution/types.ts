import { EnumMessage, ScriptType, ScriptSource, StartMode } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';

export type MenuItem = {
  key: 'none' | 'createdBy' | 'lastModifiedBy' | 'execBy' | 'last1Day' | 'last3Days' | 'last7Days' | 'scriptType';
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

export type TestInfo = {
  id: string;
  execBy: string;
  execByName: string;
  execDate: string;
  execId: string;
  execName: string;
  execStatus: EnumMessage<ExecStatus>;
  failureMessage: string;
  lastExecDate: string;
  passed: boolean;
  plugin: string;
  scriptId: string;
  scriptName: string;
  scriptSource: EnumMessage<ScriptSource>;
  scriptSourceId: string;
  scriptSourceName: string;
  scriptType: EnumMessage<ScriptType>;
  testFailureNum: string;
  testNum: string;
  usageFailedNodeId: string;
  createdDate: string;
  targetSummary: {
    disabledNum: string;
    failNum: string;
    successNum: string;
    totalNum: string;
  };
  nodeUsageSummary: {
    additionalProp1: {
      maxCpu: string;
      maxFilesystem: string;
      maxMemory: string;
      maxNetwork: string;
      meanCpu: string;
      meanFilesystem: string;
      meanMemory: string;
      meanNetwork: string;
    };
    additionalProp2: {
      maxCpu: string;
      maxFilesystem: string;
      maxMemory: string;
      maxNetwork: string;
      meanCpu: string;
      meanFilesystem: string;
      meanMemory: string;
      meanNetwork: string;
    };
    additionalProp3: {
      maxCpu: string;
      maxFilesystem: string;
      maxMemory: string;
      maxNetwork: string;
      meanCpu: string;
      meanFilesystem: string;
      meanMemory: string;
      meanNetwork: string;
    }
  };
  indicatorFunc: {
    security: boolean;
    securityCheckSetting: string;
    smoke: boolean;
    smokeCheckSetting: string;
    userDefinedSecurityAssertion: {
      actualCondition: string;
      actualExpected: string;
      assertionCondition: string;
      condition: string;
      description: string;
      enabled: boolean;
      expected: string;
      expression: string;
      extractValue: string;
      extraction: {
        apisId: string;
        defaultValue: string;
        expression: string;
        failureMessage: string;
        finalValue: string;
        location: string;
        matchItem: string;
        method: string;
        name: string;
        parameterName: string;
        request: {
          authentication: {
            apiKeys: {
              in: string;
              name: string;
              value: string;
            }[];
            description: string;
            enabled: boolean;
            name: string;
            oauth2: {
              authFlow: string;
              clientCredentials: {
                clientId: string;
                clientIn: string;
                clientSecret: string;
                password: string;
                refreshUrl: string;
                scopes: string[];
                tokenUrl: string;
                username: string;
              };
              newToken: boolean;
              password: {
                clientId: string;
                clientIn: string;
                clientSecret: string;
                password: string;
                refreshUrl: string;
                scopes: string[];
                tokenUrl: string;
                username: string;
              };
              token: string;
            };
            type: string;
            value: string;
          };
          body: {
            contentEncoding: string;
            fileName: string;
            format: string;
            forms: {
              contentEncoding: string;
              contentType: string;
              description: string;
              enabled: boolean;
              fileName: string;
              format: string;
              name: string;
              type: string;
              value: string;
            }[];
            rawContent: string;
          };
          endpoint: string;
          method: string;
          parameters: {
            description: string;
            enabled: boolean;
            format: string;
            in: string;
            name: string;
            type: string;
            value: string;
          }[];
          server: {
            description: string;
            url: string;
            variables: {
              additionalProp1: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              };
              additionalProp2: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              };
              additionalProp3: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              }
            }
          };
          url: string;
          validSecurity: boolean
        };
        value: string;
      };
      ignore: boolean;
      matchItem: string;
      name: string;
      parameterName: string;
      result: {
        failure: boolean;
        ignored: boolean;
        message: string;
      };
      success: boolean;
      type: string;
    };
    userDefinedSmokeAssertion: {
      actualCondition: string;
      actualExpected: string;
      assertionCondition: string;
      condition: string;
      description: string;
      enabled: boolean;
      expected: string;
      expression: string;
      extractValue: string;
      extraction: {
        apisId: string;
        defaultValue: string;
        expression: string;
        failureMessage: string;
        finalValue: string;
        location: string;
        matchItem: string;
        method: string;
        name: string;
        parameterName: string;
        request: {
          authentication: {
            apiKeys: {
              in: string;
              name: string;
              value: string;
            }[];
            description: string;
            enabled: boolean;
            name: string;
            oauth2: {
              authFlow: string;
              clientCredentials: {
                clientId: string;
                clientIn: string;
                clientSecret: string;
                password: string;
                refreshUrl: string;
                scopes: string[];
                tokenUrl: string;
                username: string;
              };
              newToken: boolean;
              password: {
                clientId: string;
                clientIn: string;
                clientSecret: string;
                password: string;
                refreshUrl: string;
                scopes: string[];
                tokenUrl: string;
                username: string;
              };
              token: string;
            };
            type: string;
            value: string;
          };
          body: {
            contentEncoding: string;
            fileName: string;
            format: string;
            forms: {
              contentEncoding: string;
              contentType: string;
              description: string;
              enabled: boolean;
              fileName: string;
              format: string;
              name: string;
              type: string;
              value: string;
            }[];
            rawContent: string;
          };
          endpoint: string;
          method: string;
          parameters: {
            description: string;
            enabled: boolean;
            format: string;
            in: string;
            name: string;
            type: string;
            value: string;
          }[];
          server: {
            description: string;
            url: string;
            variables: {
              additionalProp1: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              };
              additionalProp2: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              };
              additionalProp3: {
                allowableValues: string[];
                defaultValue: string;
                description: string;
              }
            }
          };
          url: string;
          validSecurity: boolean
        };
        value: string;
      };
      ignore: boolean;
      matchItem: string;
      name: string;
      parameterName: string;
      result: {
        failure: boolean;
        ignored: boolean;
        message: string;
      };
      success: boolean;
      type: string;
    }
  };
  indicatorPerf: {
    art: string;
    duration: {
      unit: string;
      value: string;
    };
    errorRate: string;
    percentile: string;
    rampUpInterval: {
      unit: string;
      value: string;
    };
    rampUpThreads: string;
    threads: string;
    tps: string;
  };
  indicatorStability: {
    art: string;
    cpu: string;
    disk: string;
    duration: {
      unit: string;
      value: string;
    };
    errorRate: string;
    memory: string;
    network: string;
    percentile: string;
    threads: string;
    tps: string;
  };
  caseResults: {
    apisId: string;
    assertionSummary: {
      disabledNum: string;
      failureNum: string;
      ignoreNum: string;
      successNum: string;
      totalNum: string;
    };
    caseId: string;
    caseName: string;
    caseType: string;
    createdDate: string;
    enabled: boolean;
    execBy: string;
    execByName: string;
    execId: string;
    execName: string;
    execStatus: string;
    failureMessage: string;
    id: string;
    lastExecDate: string;
    passed: boolean;
    plugin: string;
    sampleContent:
      {
        iteration: string;
        key: string;
        name: string;
        nodeId: string;
        sampleResult: {
          assertions: {
            actualCondition: string;
            actualExpected: string;
            assertionCondition: string;
            condition: string;
            description: string;
            enabled: boolean;
            expected: string;
            expression: string;
            extractValue: string;
            extraction: { [key: string]: any };
            ignore: boolean;
            matchItem: string;
            name: string;
            parameterName: string;
            result: {
              failure: boolean;
              ignored: boolean;
              message: string;
            };
            success: boolean;
            type: string;
          }[];
          failMessage: string;
          name: string;
          success: boolean;
          target: string;
        };
        timestamp: string;
        timestamp0: string;
      }[];
    scriptId: string;
    testFailureNum: string;
    testNum: string;
  }[];
  caseSummary: {
    disabledNum: string;
    failNum: string;
    successNum: string;
    totalNum: string;
  };
  sampleContent: {
    iteration: string;
    key: string;
    name: string;
    nodeId: string;
    sampleResult: {
      assertions: {
        actualCondition: string;
        actualExpected: string;
        assertionCondition: string;
        condition: string;
        description: string;
        enabled: boolean;
        expected: string;
        expression: string;
        extractValue: string;
        extraction: { [key: string]: any };
        ignore: boolean;
        matchItem: string;
        name: string;
        parameterName: string;
        result: {
          failure: boolean;
          ignored: boolean;
          message: string;
        };
        success: boolean;
        type: string;
      }[];
      failMessage: string;
      name: string;
      success: boolean;
      target: string;
    };
    timestamp: string;
    timestamp0: string;
  }[];
  sampleSummary: {
    brps: string;
    bwps: string;
    duration: string;
    endTime: string;
    errorRate: string;
    errors: string;
    extCounter1: string;
    extCounter2: string;
    extCounter3: string;
    extGauge1: string;
    extGauge2: string;
    extGauge3: string;
    finish: boolean;
    iterations: string;
    n: string;
    name: string;
    operations: string;
    ops: string;
    readBytes: string;
    startTime: string;
    threadMaxPoolSize: string;
    threadPoolActiveSize: string;
    threadPoolSize: string;
    threadRunning: boolean;
    threadTerminated: boolean;
    tps: string;
    tranMax: string;
    tranMean: string;
    tranMin: string;
    tranP50: string;
    tranP75: string;
    tranP90: string;
    tranP95: string;
    tranP99: string;
    tranP999: string;
    transactions: string;
    uploadResultBytes: string;
    uploadResultProgress: string;
    uploadResultTotalBytes: string;
    writeBytes: string;
  };
};

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
