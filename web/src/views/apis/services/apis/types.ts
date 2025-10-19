import { i18n } from '@xcan-angus/infra';
import { ApisListInfo } from '@/views/apis/services/protocol/types';
import { ApiPermission, ServicesPermission } from '@/enums/enums';

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
  auth?: ApiPermission | ServicesPermission,
  disabled?: boolean,
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
    auth: ApiPermission.MODIFY
  },
  {
    icon: 'icon-zhibiao',
    name: t('service.apis.navs.performance'),
    value: DrawerType.PERFORMANCE,
    disabled: true,
    auth: ApiPermission.MODIFY
  },
  {
    icon: 'icon-zhihangceshi',
    name: t('service.apis.navs.testInfo'),
    value: DrawerType.TEST_INFO,
    auth: ApiPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-yongliku',
    name: t('common.useCase'),
    value: DrawerType.CASE,
    auth: ApiPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.navs.activity'),
    value: DrawerType.ACTIVITY,
    auth: ApiPermission.VIEW,
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.navs.agent'),
    value: DrawerType.PROXY,
    auth: ApiPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-daimashitu',
    name: t('service.apis.navs.code'),
    value: DrawerType.CODE,
    auth: ApiPermission.VIEW,
    disabled: true
  },
  {
    icon: 'icon-mockjiedian',
    name: t('service.apis.navs.apiMock'),
    value: DrawerType.API_MOCK,
    auth: ApiPermission.VIEW,
    disabled: true
  }
];

export const socketNavs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.socketNavs.apiInfo'),
    value: DrawerType.API_INFO,
    auth: ApiPermission.MODIFY
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.socketNavs.activity'),
    value: DrawerType.ACTIVITY,
    auth: ApiPermission.VIEW,
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.socketNavs.agent'),
    value: DrawerType.PROXY,
    auth: ApiPermission.MODIFY,
    disabled: true
  }
];

export const serviceNav: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.serviceNav.projectInfo'),
    value: DrawerType.SERVICE_INFO,
    auth: ServicesPermission.GRANT,
    disabled: true
  },
  {
    icon: 'icon-wendangxinxi',
    name: t('service.apis.serviceNav.openapi'),
    value: DrawerType.OPENAPI,
    auth: ServicesPermission.GRANT,
    disabled: true
  },
  {
    icon: 'icon-peizhifuwutongbu',
    name: t('service.apis.serviceNav.syncConfig'),
    value: DrawerType.SYNC_CONFIG,
    auth: ServicesPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-renzhengtou',
    name: t('service.apis.serviceNav.security'),
    value: DrawerType.SECURITY,
    auth: ServicesPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-host',
    name: t('service.apis.serviceNav.serverConfig'),
    value: DrawerType.SERVER_CONFIG,
    auth: ServicesPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.serviceNav.activity'),
    auth: ServicesPermission.VIEW,
    value: DrawerType.ACTIVITY,
    disabled: true
  },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.serviceNav.agent'),
    value: DrawerType.PROXY,
    auth: ServicesPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-biaoqian',
    name: t('common.tag'),
    value: DrawerType.TAG,
    auth: ServicesPermission.MODIFY,
    disabled: true
  },
  {
    icon: 'icon-zujian',
    name: t('service.apis.serviceNav.component'),
    value: DrawerType.COMPONENT,
    auth: ServicesPermission.MODIFY,
    disabled: true
  }
];
