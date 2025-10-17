import { EnumMessage, i18n } from '@xcan-angus/infra';
import { ref } from 'vue';
import {  ApiSource, ApiStatus, ServicesCompType } from '@/enums/enums';
import { OpenAPIV3_1 } from '@/types/openapi-types';

const t = i18n.getI18n()?.global?.t || ((value: string): string => value);

export type ServicesInfo = {
  id: string;
  projectId: string;
  name: string;
  auth: boolean;
  status: EnumMessage<ApiStatus>;
  hasApis: boolean;
  mockServiceId: string;
  apisNum: number;
  createdBy: string;
  createdDate: string;
}

export type ServiceSchemaDetail = {
  serviceId: number;
  openapi: string;
  info: OpenAPIV3_1.InfoObject;
  externalDocs: OpenAPIV3_1.ExternalDocumentationObject;
  servers: OpenAPIV3_1.ServerObject[];
  security: OpenAPIV3_1.SecurityRequirementObject[];
  tags: OpenAPIV3_1.TagObject[];
  extensions: Record<string, any>;
  specVersion: string;
  lastModifiedBy: number;
  lastModifiedDate: string;
}

export type ServicesDetail = {
  id: string;
  projectId: string;
  source: EnumMessage<ApiSource>;
  importSource: EnumMessage<string>;
  name: string;
  auth: boolean;
  status: EnumMessage<ApiStatus>;
  hasApis: boolean;
  mockServiceId: string;
  apisNum: number;
  apisCaseNum: number;
  schema: ServiceSchemaDetail;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
}

export type ServicesCompDetail = {
  id: string;
  serviceId: string;
  type: EnumMessage<ServicesCompType>;
  key: string;
  ref: string;
  model?: any;
  description?: string;
  resolvedRefModels?: Record<string, string>;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;

  // Temp fields in web
  isQuote: boolean; // whether the component is a reference to another
  ellipsis: boolean; // UI hint flags
  showEllipsis: boolean; // UI hint flags
  isEdit: boolean; // whether current item is in edit state (UI)
  copyLoading: boolean; // UI: copy action loading
  delLoading: boolean; // UI: delete action loading
  quoteName?: string; // last referenced name chain
  isAdd: boolean;
  isExpand: boolean;
  saveLoading: boolean;
  keyErr: boolean;
  hasModel:boolean;
}

export interface ExternalDocInfo extends OpenAPIV3_1.ExternalDocumentationObject{
  // Temp fields in web
  urlErr: {
    emptyUrl: boolean;
    errUrl: boolean;
  };
  ellipsis?: boolean;
  showEllipsis?:boolean;
}

export interface TagInfo extends OpenAPIV3_1.TagObject{
  name: string;
  description?: string;
  externalDocs: ExternalDocInfo;

  // Temp fields in web
  id: string;
  isEdit: boolean;
  isExpand: boolean;
  isAdd: boolean;
  delLoading: boolean;
  saveLoading: boolean;
  nameErr: boolean;
  ellipsis: boolean;
  showEllipsis:boolean;
}

export interface ApiKeyExtensionInfo extends OpenAPIV3_1.ApiKeyExtensionField {
  // Temp feilds in web
  nameErr: boolean;
  valueErr: boolean;
}

export type SecuritySchemeInfo = OpenAPIV3_1.SecuritySchemeObject & {
  // Temp feilds in web
  value: string;
  username: string;
  password: string;
  token: string;
  apiKeyList: ApiKeyExtensionInfo[];
  usernameErr?: boolean;
  passwordErr?: boolean;
  tokenErr?: boolean;
  oauth2TokenErr?: boolean;
  refreshUrlErr: {
    isEmpty: boolean,
    isError: boolean
  };
  callbackUrlErr: {
    isEmpty: boolean,
    isError: boolean
  };
  tokenUrlErr: {
    isEmpty: boolean,
    isError: boolean
  };
  authorizationUrlErr: {
    isEmpty: boolean,
    isError: boolean
  };
  scopesErr?: boolean;
}

export interface ModalsConfig {
  syncModalVisible: boolean,
  serverUrlModalVisible: boolean,
  importModalVisible: boolean,
  authenticateModalVisible: boolean,
  exportInterfaceModalVisible: boolean,
  testScriptVisible: boolean;
  shareModalVisible: boolean,
  authModalVisible: boolean,
  activeId: string,
  activeName: string,
  statusVisible: boolean;
  auth: boolean;
  selectedNode?:ServicesInfo;
  delTestScriptVisible: boolean;
  enabledApiTestVisible: boolean;
}

export type UnarchivedItem = {
  id: string;
  protocol: {
    value: string;
    message: string;
  },
  method: string;
  endpoint: string;
  summary: string;
  createdDate: string;
  lastModifiedDate: string;
}

export type FoldActionKey = 'creatService' | 'import' | 'export'|'authorization';

export const globalActions = {
  name: t('service.sidebar.topAction.addService'),
  menuItems: [
    {
      name: t('actions.import'),
      key: 'import',
      icon: 'icon-shangchuan'
    },
    {
      name: t('actions.export'),
      key: 'export',
      icon: 'icon-daochu1'
    },
    {
      name: t('actions.permission'),
      key: 'authorization',
      icon: 'icon-quanxian1'
    }
  ]
};

export const foldGlobalActions = ref<{ name: string; key: FoldActionKey; icon: string; }[]>([
  {
    name: t('service.sidebar.foldAction.addService'),
    key: 'creatService',
    icon: 'icon-chuangjianfuwu'
  },
  {
    name: t('service.sidebar.foldAction.localImport'),
    key: 'import',
    icon: 'icon-shangchuan'
  },
  {
    name: t('actions.export'),
    key: 'export',
    icon: 'icon-daochu1'
  },
  {
    name: t('actions.permission'),
    key: 'authorization',
    icon: 'icon-quanxian1'
  }
]);

export const menuActions = [
  {
    name: t('service.sidebar.actions.addApi'),
    icon: 'icon-tianjiajiekou',
    key: 'add',
    permission: 'ADD',
    disabled: false,
    children: [
      {
        name: t('service.sidebar.actions.httpApi'),
        key: 'addApi',
        permission: 'ADD',
        disabled: false
      },
      {
        name: t('service.sidebar.actions.webSocketApi'),
        key: 'addSocket',
        permission: 'ADD',
        disabled: false
      }
    ]
  },
  {
    name: t('service.sidebar.actions.syncConfig'),
    icon: 'icon-peizhifuwutongbu',
    key: 'sync-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.securityConfig'),
    icon: 'icon-renzhengtou',
    key: 'authentication-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.serverConfig'),
    icon: 'icon-host',
    key: 'server-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.localImport'),
    icon: 'icon-daoru',
    key: 'local-import',
    permission: 'ADD',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.exportApi'),
    icon: 'icon-daochujiekou',
    key: 'export-apis',
    permission: 'EXPORT',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.mockService'),
    permission: 'VIEW',
    key: 'mock',
    icon: 'icon-mockjiedian',
    disabled: false
  },
  {
    name: t('actions.rename'),
    icon: 'icon-bianji',
    key: 'rename',
    permission: 'RENAME',
    disabled: false
  },
  {
    name: t('actions.delete'),
    icon: 'icon-qingchu',
    key: 'delete',
    permission: 'DELETE',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.translate'),
    icon: 'icon-yuyan',
    key: 'translate',
    disabled: false
  },
  {
    name: t('actions.clone'),
    icon: 'icon-fuzhi',
    key: 'clone',
    permission: 'CLONE',
    disabled: false
  },
  {
    name: t('actions.permission'),
    icon: 'icon-quanxian1',
    key: 'auth',
    permission: 'GRANT',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.modifyStatus'),
    key: 'setStatus',
    icon: 'icon-fabu',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.batchModifyParams'),
    key: 'batchModify',
    icon: 'icon-xiugai',
    permission: 'MODIFY',
    disabled: false,
    children: [
      {
        name: t('service.sidebar.actions.batchAddParams'),
        key: 'batchAddParams',
        icon: 'icon-piliangtianjiacanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyParams'),
        key: 'batchModifyParams',
        icon: 'icon-piliangxiugaicanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelParams'),
        key: 'batchDelParams',
        icon: 'icon-piliangshanchucanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchEnabledParams'),
        key: 'batchEnabledParams',
        icon: 'icon-piliangqiyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDisabledParams'),
        key: 'batchDisabledParams',
        icon: 'icon-piliangjinyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyAuth'),
        key: 'batchModifyAuth',
        icon: 'icon-piliangxiugairenzheng',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyServer'),
        key: 'batchModifyServer',
        icon: 'icon-piliangxiugaifuwuqi',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchLinkVariable'),
        key: 'batchLinkVariable',
        icon: 'icon-piliangyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelVariable'),
        key: 'batchDelVariable',
        icon: 'icon-piliangquxiaoyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchLinkDataSet'),
        key: 'batchLinkDataSet',
        icon: 'icon-piliangyinyongshujuji',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelDataSet'),
        key: 'batchDelDataSet',
        icon: 'icon-piliangquxiaoyinyongshujuji',
        permission: 'MODIFY'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.apiTestScript'),
    key: 'testScript',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.generateTestScript'),
        key: 'setTestScript',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben',
        tip: t('service.sidebar.actions.generateTestScriptTip')
      },
      {
        name: t('service.sidebar.actions.deleteTestScript'),
        key: 'delTestScript',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben',
        tip: t('service.sidebar.actions.deleteTestScriptTip')
      }
    ]
  },
  {

    name: t('service.sidebar.actions.enableDisableTest'),
    key: 'enabledTest',
    disabled: false,
    permission: 'MODIFY',
    icon: 'icon-zhibiao'
  },
  {
    name: t('service.sidebar.actions.execServiceTest'),
    key: 'execService',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.execSmokeTest'),
        key: 'funcTestExecSmoke',
        permission: 'TEST',
        icon: 'icon-gengxinceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execSecurityTest'),
        key: 'funcTestExecSecurity',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.execApiTest'),
    key: 'exec',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.execFuncTest'),
        key: 'funcTestExec',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execPerfTest'),
        key: 'perfTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execStabilityTest'),
        key: 'stabilityTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.apiTestTask'),
    key: 'testTask',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshirenwu',
    children: [
      {
        name: t('service.sidebar.actions.generateTestTask'),
        key: 'setTest',
        icon: 'icon-shengchengceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.generateTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.restartTestTask'),
        key: 'reTest',
        icon: 'icon-zhongxinkaishiceshi',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.restartTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.reopenTestTask'),
        key: 'reopen',
        icon: 'icon-zhongxindakaiceshirenwu',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.reopenTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.deleteTestTask'),
        key: 'deleteTask',
        icon: 'icon-shanchuceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.deleteTestTaskTip')
      }
    ]
  }
];
