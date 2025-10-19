import { AssertionCondition, AssertionType, ExtractionMethod } from '@xcan-angus/infra';

export type Extraction = {
  method: ExtractionMethod | undefined;
  expression: string | undefined;
  location: string | undefined;
  matchItem: string | undefined;
  apisId?: string;
  defaultValue?: string;
  failureMessage?: string;
  finalValue?: string;
  request?: Request;
  name?: string;
  parameterName?: string;
  value?: string;
}

export type ConditionResult = {
  failure: boolean;// 执行结果
  name: string;// 提取的变量名
  conditionMessage: string;// 条件表达式格式错误原因
  failureMessage: string;// 提取失败原因
  value: string;// 变量的值
  ignored: boolean;// 是否忽略该条断言
  message: string;
}

export type AssertConfig = {
  assertionCondition: AssertionCondition;
  condition: string;
  description: string;
  enabled: boolean;
  expected: string;
  extraction: Extraction;
  parameterName: string;
  name: string;
  type: AssertionType;
  result?: {
    failure: boolean;
    message: string;
  };
}

export type AssertResult = {
  name: string;
  expected?: string;
  extractValue?: string;
  type: { message: string; value: AssertionCondition; } | AssertionCondition;
  parameterName: string;
  condition: string;
  extraction: boolean;// 是否是提取期望值
  assertionCondition: AssertionCondition;
  result: {
    ignored?: boolean;
    expectedData: {
      data: string | null;
      message: string;
      errorMessage: string;
    };
    failure: boolean;
    realValueData: {
      data: string | null;
      message: string;
      errorMessage: string;
    };
    message: string;
  };
  _condition: {
    failure: boolean;// 执行结果
    name: string;// 提取的变量名
    conditionMessage: string;// 断言表达式错误的原因
    failureMessage: string;// 提取失败的原因
    value: string;// 提取变量的值
    ignored: boolean;// 是否忽略该条断言
    message: string;
  };
};

// TODO 替换参数
export type Parameter = {
  form: { [key: string]: any };
  rawBody: { [key: string]: any };
  responseBody: {
    data: any;
    size: number;
  };
  query: { [key: string]: any };
  path: { [key: string]: any };
  header: { [key: string]: any };
  duration: number;
  responseHeader: Record<string, string>;
  status: number;
}

// TODO 替换参数
export interface ParamsInfo {
  name: string;
  in: 'path' | 'query';
  description: string;
  key?: symbol;
  [key: string]: any; // TODO ?? 未约定的字段，前端临时字段吗
}

export interface RequestSetting {
  enableParamValidation: boolean;
  connectTimeout: number;
  readTimeout: number;
  maxRedirects: number;
  retryNum: number;
}
