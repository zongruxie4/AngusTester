export type FormState = {
  name: string;
  scriptId: string | undefined;
}

export type Configuration = {
  iterations: string | undefined;
  duration: string;
  thread: {
    threads: string;
    rampDownInterval: string;
    rampDownThreads: string;
    rampUpInterval: string;
    rampUpThreads: string;
    extensions?: any;
  };
  onError: {
    action: 'CONTINUE' | 'STOP' | 'STOP_NOW';
    sampleError: boolean;
    sampleErrorNum: string;
  },
  startMode: string;
  startAtDate: string; // 定时执行时间，当 startMode=TIMING时必须
  shutdownTimeout: string; // 停止执行超时时间，如果超过指定超时时间采样未结束，会强制Kill掉执行，可能导致采样错误
  nodeSelectors: { // 节点选择
    num: string; // 执行任务所需节点数
    // 选择策略
    nodeIds: string[]; // 指定节点
    maxTaskNum: string; // 执行节点允许的最大任务数，超过时忽略选择，最大1000
    lastExecuted: false; // 选择最后一次执行当前脚本的节点，第一次执行时忽略匹配
    // 按照规格选择节点
    cpuSpec: string; // 最小CPU规格，最大 64
    memorySpec: string; // 最小内存规格，最大 512GB
    diskSpec: string; // 最小磁盘规格，最大 2000GB
    // 安装使用率选择节点
    cpuIdleRate: string; // CPU空闲率，最大100%
    memoryIdleRate: string; // 内存空闲率，最大100%
    diskIdleRate: string; // 磁盘空闲率，最大100%
  },
  reportInterval: string; // 报告采样结果的间隔。支持范围：1-300秒，默认为5秒
};

// 获得仨数的最大公约数
export const gcdThreeNum = (a, b, c) => {
  let min = Math.min(a, b);
  min = Math.min(min, c);
  for (let i = min; i > 0; i--) {
    if (a % i === 0 && b % i === 0 && c % i === 0) {
      return i;
    }
  }
};

export type TargetType = 'API' | 'PROJECT' | 'SCENARIO' | 'SERVICE';
export type Scope = 'CURRENT' | 'GLOBAL' | 'INHERIT';

export type EnumObj = { value: string; checked: boolean; valueErr: boolean; }
export type MethodObj = 'EXACT_VALUE' | 'REGEX' | 'JSON_PATH' | 'X_PATH';

export type VariableObj = {
  createdBy: string;
  creator: string;
  createdDate: string;
  description: string;
  elementName: string;
  enabled: boolean;
  extraction: {
    defaultValue: string;
    expression: string;
    finalValue: string;
    location: string;
    method: {value:MethodObj, message:string};
    name: string;
    parameterName: string;
    value: string;
    projectId?: string;
    defaultProject?: {id:string, name:string};
    apiAction?: string;
    failureMessage?: string;
    apisId?: any;
    request?: any;
    parameterNameErr?: boolean;
    expressionErr?: boolean;
    requestErr?: number;
  },
  id: string;
  modifiedBy: string;
  modifier: string;
  modifiedDate: string;
  name: string;
  scope: {value:Scope, message:string};
  source: {value:string, message:string};
  targetId: string;
  targetName: string;
  targetType: {value:TargetType, message:string};
  value: string;
  nameErr: boolean;
  valueErr: boolean;
  type: boolean;
}

export type ListVariableObj = {
  createdBy: string;
  creator: string;
  createdDate: string;
  description: string;
  elementName: string;
  enabled: boolean;
  extraction: {
    defaultValue: string;
    expression: string;
    finalValue: string;
    location: string;
    method: {value:MethodObj, message:string};
    name: string;
    parameterName: string;
    value: string;
    projectId?: string;
    defaultProject?: {id:string, name:string};
    apiAction?: string;
    failureMessage?: string;
    apisId?: any;
    request?: any;
    parameterNameErr?: boolean;
    expressionErr?: boolean;
    requestErr?: number;
  },
  id: string;
  modifiedBy: string;
  modifier: string;
  modifiedDate: string;
  name: string;
  scope: {value:Scope, message:string};
  source: string;
  targetId: string;
  targetName: string;
  targetType: {value:TargetType, message:string};
  value: string;
  nameErr: boolean;
  valueErr: boolean;
  delVisible: boolean;
  type: boolean;
}

export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';

export type RequestConfig = {
    method: HttpMethod;
    url: string;
    server: {
        url: string;
        variables?: {
            [key: string]: {
                defaultValue: string;
                allowableValues: string[];
                description?: string;
            }
        };
        description?: string;
    };
    endpoint: string;
    parameters:{
        name: string;
        in: ParameterIn;
        value: string;
        enabled:boolean;
        type:'string';
    }[];
    body: {[key:string]:{[key:string]:any}};
    authentication:{
        type: string;
        enabled: boolean;
        'x-xc-value': string;
        'x-scheme': string;
    };
}

export type AvailableServer = {
    url: string;
    description?: string;
    variables?: {
        [key: string]: {
            default: string;
            description: string;
            enum: string[];
        }
    };
}

export type RequestBody = {
    $ref:string;
    description: string;
    content: {
        [key:string]: {
            schema: {[key:string]:any};
            exampleSet: boolean;
            'x-xc-value': string;
        }
    };
    required: boolean;
}

export type Authentication = {
    type: string;
    enabled: boolean;
    'x-xc-value': string;
    'x-scheme': string;
    $ref?:string;
}
