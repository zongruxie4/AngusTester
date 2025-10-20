import { AssertionCondition, AssertionType, ExtractionMethod } from '@xcan-angus/infra';

/**
 * Extraction configuration type
 * <p>
 * Defines the structure for data extraction configuration
 * </p>
 */
export type Extraction = {
  /** Extraction method to use */
  method: ExtractionMethod | undefined;
  /** Extraction expression */
  expression: string | undefined;
  /** Location to extract from */
  location: string | undefined;
  /** Item to match */
  matchItem: string | undefined;
  /** API ID for reference */
  apisId?: string;
  /** Default value if extraction fails */
  defaultValue?: string;
  /** Failure message */
  failureMessage?: string;
  /** Final extracted value */
  finalValue?: string;
  /** Request object */
  request?: Request;
  /** Extraction name */
  name?: string;
  /** Parameter name */
  parameterName?: string;
  /** Value to extract */
  value?: string;
}

/**
 * Condition result type
 * <p>
 * Defines the structure for condition execution results
 * </p>
 */
export type ConditionResult = {
  /** Execution result - whether the condition failed */
  failure: boolean;
  /** Extracted variable name */
  name: string;
  /** Error message for condition expression format */
  conditionMessage: string;
  /** Failure reason for extraction */
  failureMessage: string;
  /** Value of the variable */
  value: string;
  /** Whether to ignore this assertion */
  ignored: boolean;
  /** General message */
  message: string;
}

/**
 * Assertion configuration type
 * <p>
 * Defines the structure for assertion configuration
 * </p>
 */
export type AssertConfig = {
  /** Assertion condition type */
  assertionCondition: AssertionCondition;
  /** Condition expression */
  condition: string;
  /** Description of the assertion */
  description: string;
  /** Whether the assertion is enabled */
  enabled: boolean;
  /** Expected value */
  expected: string;
  /** Extraction configuration */
  extraction: Extraction;
  /** Parameter name */
  parameterName: string;
  /** Assertion name */
  name: string;
  /** Assertion type */
  type: AssertionType;
  /** Execution result */
  result?: {
    /** Whether the assertion failed */
    failure: boolean;
    /** Result message */
    message: string;
  };
}

/**
 * Assertion result type
 * <p>
 * Defines the structure for assertion execution results
 * </p>
 */
export type AssertResult = {
  /** Assertion name */
  name: string;
  /** Expected value */
  expected?: string;
  /** Extracted value */
  extractValue?: string;
  /** Assertion type with message or condition */
  type: { message: string; value: AssertionCondition; } | AssertionCondition;
  /** Parameter name */
  parameterName: string;
  /** Condition expression */
  condition: string;
  /** Whether this is extracting expected value */
  extraction: boolean;
  /** Assertion condition type */
  assertionCondition: AssertionCondition;
  /** Execution result details */
  result: {
    /** Whether the assertion was ignored */
    ignored?: boolean;
    /** Expected data information */
    expectedData: {
      /** Expected data value */
      data: string | null;
      /** Data message */
      message: string;
      /** Error message */
      errorMessage: string;
    };
    /** Whether the assertion failed */
    failure: boolean;
    /** Real value data information */
    realValueData: {
      /** Real data value */
      data: string | null;
      /** Data message */
      message: string;
      /** Error message */
      errorMessage: string;
    };
    /** Result message */
    message: string;
  };
  /** Condition execution details */
  _condition: {
    /** Execution result - whether the condition failed */
    failure: boolean;
    /** Extracted variable name */
    name: string;
    /** Error message for assertion expression */
    conditionMessage: string;
    /** Failure reason for extraction */
    failureMessage: string;
    /** Extracted variable value */
    value: string;
    /** Whether to ignore this assertion */
    ignored: boolean;
    /** General message */
    message: string;
  };
};

/**
 * Parameter type for API testing
 * <p>
 * Defines the structure for API test parameters
 * </p>
 */
export type Parameter = {
  /** Form data parameters */
  form: { [key: string]: any };
  /** Raw body data */
  rawBody: { [key: string]: any };
  /** Response body information */
  responseBody: {
    /** Response data */
    data: any;
    /** Response size */
    size: number;
  };
  /** Query parameters */
  query: { [key: string]: any };
  /** Path parameters */
  path: { [key: string]: any };
  /** Header parameters */
  header: { [key: string]: any };
  /** Request duration */
  duration: number;
  /** Response headers */
  responseHeader: Record<string, string>;
  /** HTTP status code */
  status: number;
}

/**
 * Parameter information interface
 * <p>
 * Defines the structure for parameter information
 * </p>
 */
export interface ParamsInfo {
  /** Parameter name */
  name: string;
  /** Parameter location (path or query) */
  in: 'path' | 'query';
  /** Parameter description */
  description: string;
  /** Unique key for tracking */
  key?: symbol;
  /** Additional properties */
  [key: string]: any;
}

/**
 * Request setting interface
 * <p>
 * Defines the structure for API request settings
 * </p>
 */
export interface RequestSetting {
  /** Whether to enable parameter validation */
  enableParamValidation: boolean;
  /** Connection timeout in milliseconds */
  connectTimeout: number;
  /** Read timeout in milliseconds */
  readTimeout: number;
  /** Maximum number of redirects */
  maxRedirects: number;
  /** Number of retries */
  retryNum: number;
}
