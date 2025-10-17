/**
 * Form state interface for execution settings
 */
export type FormState = {
  name: string;
  scriptId: string | undefined;
}

/**
 * Execution configuration interface
 */
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
  };
  startMode: string;
  startAtDate: string; // Scheduled execution time, required when startMode=TIMING
  shutdownTimeout: string; // Stop execution timeout, if exceeded will force kill execution
  nodeSelectors: { // Node selection
    num: string; // Number of nodes required for task execution
    // Selection strategy
    nodeIds: string[]; // Specified nodes
    maxTaskNum: string; // Maximum tasks allowed on execution nodes, max 1000
    lastExecuted: false; // Select nodes that last executed current script
    // Select nodes by specification
    cpuSpec: string; // Minimum CPU specification, max 64
    memorySpec: string; // Minimum memory specification, max 512GB
    diskSpec: string; // Minimum disk specification, max 2000GB
    // Select nodes by idle rate
    cpuIdleRate: string; // CPU idle rate, max 100%
    memoryIdleRate: string; // Memory idle rate, max 100%
    diskIdleRate: string; // Disk idle rate, max 100%
  };
  reportInterval: string; // Report sampling result interval, range: 1-300 seconds, default 5 seconds
};

/**
 * Calculate the greatest common divisor of three numbers
 */
export const calculateGreatestCommonDivisor = (firstNumber: number, secondNumber: number, thirdNumber: number): number => {
  let minimumValue = Math.min(firstNumber, secondNumber);
  minimumValue = Math.min(minimumValue, thirdNumber);
  for (let i = minimumValue; i > 0; i--) {
    if (firstNumber % i === 0 && secondNumber % i === 0 && thirdNumber % i === 0) {
      return i;
    }
  }
  return 1;
};

/**
 * Target type enumeration
 */
export type TargetType = 'API' | 'PROJECT' | 'SCENARIO' | 'SERVICE';

/**
 * Scope enumeration
 */
export type Scope = 'CURRENT' | 'GLOBAL' | 'INHERIT';

/**
 * Enum object interface
 */
export type EnumObj = { value: string; checked: boolean; valueErr: boolean; };

/**
 * Method object enumeration
 */
export type MethodObj = 'EXACT_VALUE' | 'REGEX' | 'JSON_PATH' | 'X_PATH';

/**
 * Variable object interface for execution settings
 */
export type VariableObj = {
  createdBy: string;
  createdByName: string;
  createdDate: string;
  description: string;
  elementName: string;
  enabled: boolean;
  extraction: {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    location: string;
    method: {value: MethodObj, message: string};
    name: string;
    parameterName: string;
    value: string;
    parameterNameErr?: boolean;
    expressionErr?: boolean;
  };
  id: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  scope: {value: Scope, message: string};
  targetId: string;
  targetName: string;
  targetType: {value: TargetType, message: string};
  value: string;
  isEdit: boolean;
  isExpand: boolean;
  isAdd: boolean;
  delLoading: boolean;
  saveLoading: boolean;
  nameErr: boolean;
  valueErr: boolean;
  type: boolean;
  enableLoading: boolean;
};
