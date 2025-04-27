import apiUtils from 'angus-design/utils';

export const { API_EXTENSION_KEY } = apiUtils;

const { valueKey, enabledKey } = API_EXTENSION_KEY;

export type FormState = {
  attachments: {
    name: string;
    url: string;
  }[],
  caseType: 'FUNC' | 'APIS'
  deadlineDate: string;
  description: string;
  evalWorkload: string;
  actualWorkload: string;
  moduleId?: string;
  name: string;
  dirId: string;
  planId: string;
  precondition?: string;
  priority: string;
  steps?: {
    expectedResult: string;
    step: string;
  } [],
  tagIds: string[];
  testerId: string;
  refIdMap:{
    TASK:string[];
    CASE:string[];
  };
  apisId?: string;
  assertions?: Record<string, any>[];
  authentication?: Record<string, any>;
  method?: string;
  parameters?: Record<string, any>[];
  requestBody?: Record<string, any>;
  endpoint?: string;
  projectId?: string;
  currentServer?: { url: '' }
}

export const ToolBarMenus = [
  {
    name: '基本',
    value: 'request'
  },
  {
    name: '响应',
    value: 'response'
  },
  {
    name: '耗时分析',
    value: 'time'
  },
  {
    name: 'Cookie',
    value: 'cookie'
  },
  {
    name: '断言结果',
    value: 'assert'
  }
];
export const getDefaultParams = (config = {}) => {
  return {
    name: '',
    in: 'query',
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    schema: {
      type: 'string'
    },
    ...config
  };
};
