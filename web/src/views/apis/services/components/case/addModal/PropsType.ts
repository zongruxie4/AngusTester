
import { i18n } from '@xcan-angus/infra';

import apiUtils from '@/utils/apis/index';
export const { API_EXTENSION_KEY } = apiUtils;

const { valueKey, enabledKey } = API_EXTENSION_KEY;
const t = i18n.getI18n()?.global?.t || ((value: string): string => value);

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
    name: t('service.case.addModal.toolbarMenus.request'),
    value: 'request'
  },
  {
    name: t('service.case.addModal.toolbarMenus.response'),
    value: 'response'
  },
  {
    name: t('service.case.addModal.toolbarMenus.time'),
    value: 'time'
  },
  {
    name: t('service.case.addModal.toolbarMenus.cookie'),
    value: 'cookie'
  },
  {
    name: t('service.case.addModal.toolbarMenus.assert'),
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
