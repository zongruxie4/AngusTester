import {EnumMessage, HttpMethod, i18n} from '@xcan-angus/infra';
import {ApiImportSource, ApiSource, ApisProtocol, ApiStatus} from '@/enums/enums';
import {OpenAPIV3} from '@/types/openapi-types';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

/**
 * Drawer types enum for type safety
 * Defines all possible drawer keys used in the application
 */
export enum DrawerType {
  // API related drawers
  API_INFO = 'apiInfo',
  PERFORMANCE = 'performance',
  ACTIVITY = 'activity',
  TEST_INFO = 'testInfo',
  SHARE_LIST = 'shareList',
  PROXY = 'proxy',
  CODE = 'code',
  API_MOCK = 'apiMock',
  CASE = 'case',

  // Service related drawers
  SERVICE_INFO = 'serviceInfo',
  OPENAPI = 'openapi',
  SYNC_CONFIG = 'syncConfig',
  SECURITY = 'security',
  SERVER_CONFIG = 'serverConfig',
  TAG = 'tag',
  COMPONENT = 'component',
  SOCKET_CONFIG = 'socketConfig'
}

export type NavItem = {
  icon: string,
  name: string,
  value: string,
  auth?: 'MODIFY' | 'GRANT' | 'VIEW ' | 'SHARE',
  disabled?: boolean,
}

export type ApisListInfo = {
  id: string;
  source: EnumMessage<ApiSource>;
  importSource: EnumMessage<ApiImportSource>;
  serviceId: string;
  serviceName: string;
  protocol: EnumMessage<ApisProtocol>;
  method: HttpMethod;
  endpoint: string;
  summary: string;
  operationId: string;
  // servers: Server[];
  deprecated: boolean;
  status: EnumMessage<ApiStatus>;
  favourite: boolean;
  follow: boolean;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  ownerId: string;
  ownerName: string;
  avatar: string;
  createdDate: string;
  lastModifiedDate: string;
  auth: boolean;
  serviceAuth: boolean;
  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  testFunc?: boolean;
  testFuncPassed?: boolean;
  testFuncFailureMessage: string;
  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  testPerf?: boolean;
  testPerfPassed?: boolean;
  testPerfFailureMessage: string;
  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  testStability?: boolean;
  testStabilityPassed?: boolean;
  testStabilityFailureMessage: string;
  tags: string[];
  mockServiceId: string;
  mockApisId: string;
}

export type ApisUnarchivedListInfo = {
  id: string;
  projectId: string;
  protocol: EnumMessage<ApisProtocol>;
  method: string;
  endpoint: string;
  summary: string;
  createdDate: string;
  lastModifiedDate: string;
}

// Detail VO mapped from backend class `ApisUnarchivedDetailVo`
export interface ApisUnarchivedDetail {
  id?: string;
  projectId?: string;

  protocol?: EnumMessage<ApisProtocol>;
  method?: string;
  endpoint?: string;

  // OpenAPI Document fields
  summary?: string;
  description?: string;
  operationId?: string;
  parameters?: (OpenAPIV3.ReferenceObject | OpenAPIV3.ParameterObject)[];
  requestBody?: OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject;
  responses?: OpenAPIV3.ResponsesObject;
  security?: OpenAPIV3.SecurityRequirementObject[];
  currentServer?: OpenAPIV3.ServerObject;
  extensions?: Record<string, any>;

  // Security/authentication
  authentication?: OpenAPIV3.SecuritySchemeObject;
  assertions?: any[];
  auth?: boolean;
  secured?: boolean;

  tenantId?: string;
  createdBy?: string;
  createdDate?: string;
  lastModifiedDate?: string;
}

/**
 * API list item shape displayed in the virtual list
 */
export interface APITestResult {
  id: string;
  url: string;
  method: string;
  apisName?: string;
  caseId?: string;
  caseName?: string;
  summary?: string;
  // test enabling & states
  enabledTest?: boolean;
  tested?: boolean;
  passed?: boolean;
  failed?: boolean;
  // per-type results
  funcTestPassed?: boolean;
  perfTestPassed?: boolean;
  stabilityTestPassed?: boolean;
  funcTestFailureMessage?: string;
  perfTestFailureMessage?: string;
  stabilityTestFailureMessage?: string;
  // nested result for Grid fallback
  result?: Record<string, unknown>;
  caseType?: {
    value: string
  }
}

export interface MainStateType {
  allData: Array<ApisListInfo>,
  dataSource: Array<ApisListInfo>,
  id: string,
  name: string,
  searchKeyword: string,
  order: Record<string, string | undefined>,
  serviceId: string,
  showGroupList: boolean,
  drawerComp: Array<NavItem>,
  groupedBy?: string,
  type?: 'API' | 'WEBSOCKET';
  serviceAuth: boolean
}

export const apiNavs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.navs.apiInfo'),
    value: DrawerType.API_INFO,
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhibiao',
    name: t('service.apis.navs.performance'),
    value: DrawerType.PERFORMANCE,
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhihangceshi',
    name: t('service.apis.navs.testInfo'),
    value: DrawerType.TEST_INFO,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-yongliku',
    name: t('common.useCase'),
    value: DrawerType.CASE,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.navs.activity'),
    value: DrawerType.ACTIVITY,
    auth: 'VIEW ',
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.navs.agent'),
    value: DrawerType.PROXY,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-daimashitu',
    name: t('service.apis.navs.code'),
    value: DrawerType.CODE,
    auth: 'VIEW ',
    disabled: true
  },
  {
    icon: 'icon-mockjiedian',
    name: t('service.apis.navs.apiMock'),
    value: DrawerType.API_MOCK,
    auth: 'VIEW ',
    disabled: true
  }
];

export const serviceNav: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.serviceNav.projectInfo'),
    value: DrawerType.SERVICE_INFO,
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-wendangxinxi',
    name: t('service.apis.serviceNav.openapi'),
    value: DrawerType.OPENAPI,
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-peizhifuwutongbu',
    name: t('service.apis.serviceNav.syncConfig'),
    value: DrawerType.SYNC_CONFIG,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-renzhengtou',
    name: t('service.apis.serviceNav.security'),
    value: DrawerType.SECURITY,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-host',
    name: t('service.apis.serviceNav.serverConfig'),
    value: DrawerType.SERVER_CONFIG,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.serviceNav.activity'),
    auth: 'VIEW ',
    value: DrawerType.ACTIVITY,
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.serviceNav.agent'),
    value: DrawerType.PROXY,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-biaoqian',
    name: t('common.tag'),
    value: DrawerType.TAG,
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-zujian',
    name: t('service.apis.serviceNav.component'),
    value: DrawerType.COMPONENT,
    auth: 'MODIFY',
    disabled: true
  }
];

export const socketNavs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.socketNavs.apiInfo'),
    value: DrawerType.API_INFO,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.socketNavs.activity'),
    value: DrawerType.ACTIVITY,
    auth: 'VIEW ',
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.socketNavs.agent'),
    value: DrawerType.PROXY,
    auth: 'MODIFY',
    disabled: true
  }
];
