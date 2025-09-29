import { Method } from '@/views/apis/services/apiHttp/interface';
import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export type NavItem = {
  icon: string,
  name: string,
  value: string,
  auth?: 'MODIFY' | 'GRANT' | 'VIEW ' | 'SHARE',
  disabled?: boolean,
}

export interface DataSourceType {
  endpoint: string,
  method: Method,
  summary: string,
  favourite: boolean,
  id: string,
  show: boolean,
  auth:boolean,
  protocol: {
    value: string;
    message: string;
  };
  status?: { value: string };
  serviceId?: string;
  serviceName?: string;
}

export interface StateType {
  allData: Array<DataSourceType>,
  dataSource: Array<DataSourceType>,
  id: string,
  name: string,
  order: Record<string, string | undefined>,
  serviceId: string,
  showGroupList: boolean,
  drawerComp: Array<NavItem>,
  groupedBy?: string,
  type?: 'API' | 'WEBSOCKET';
  serviceAuth: boolean
}
export const navs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.navs.apiInfo'),
    value: 'apiInfo', // 1
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhibiao',
    name: t('service.apis.navs.performance'),
    value: 'performance', // 2
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhihangceshi',
    name: t('service.apis.navs.testInfo'),
    value: 'testInfo', // 5
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-yongliku',
    name: t('common.useCase'),
    value: 'case', // 5
    auth: 'MODIFY',
    disabled: true
  },
  // {
  //   icon: 'icon-qingqiulishi',
  //   name: '接口请求历史',
  //   value: '3',
  //   disabled: true,
  //   auth: 'VIEW',
  //   component: defineAsyncComponent(() => import('./slider/apis/history/index.vue'))
  // },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.navs.activity'),
    value: 'activity', // 4
    auth: 'VIEW ',
    disabled: true
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: '变量',
  //   value: 'variable', // 6
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  // {
  //   icon: 'icon-fenxiang',
  //   name: '接口分享记录',
  //   value: 'shareList', // 7
  //   auth: 'SHARE',
  //   disabled: true
  // },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.navs.agent'),
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-daimashitu',
    name: t('service.apis.navs.code'),
    value: 'code',
    auth: 'VIEW ',
    disabled: true
  },
  {
    icon: 'icon-mockjiedian',
    name: t('service.apis.navs.apiMock'),
    value: 'apiMock',
    auth: 'VIEW ',
    disabled: true
  }
];

export const serviceNav: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.serviceNav.projectInfo'),
    value: 'projectInfo', // 1
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-wendangxinxi',
    name: t('service.apis.serviceNav.openapi'),
    value: 'openapi',
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-peizhifuwutongbu',
    name: t('service.apis.serviceNav.syncConfig'),
    value: 'syncConfig', // 6
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-renzhengtou',
    name: t('service.apis.serviceNav.security'),
    value: 'security', // 7
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-host',
    name: t('service.apis.serviceNav.serverConfig'),
    value: 'serverConfig', // 8
    auth: 'MODIFY',
    disabled: true
  },
  // {
  //   icon: 'icon-zhibiao',
  //   name: '测试指标',
  //   value: 'perf', // 2
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  // {
  //   icon: 'icon-zhihangceshi',
  //   name: '测试结果',
  //   value: 'testInfo', // 3
  //   auth: 'VIEW',
  //   disabled: true
  // },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.serviceNav.activity'),
    auth: 'VIEW ',
    value: 'activity',
    disabled: true
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: '变量',
  //   value: 'variable', // 4
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  // {
  //   icon: 'icon-fenxiang',
  //   name: '分享记录',
  //   value: 'shareList', // 5
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.serviceNav.agent'),
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-biaoqian',
    name: t('common.tag'),
    value: 'tag', // 9
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-zujian',
    name: t('service.apis.serviceNav.componnet'),
    value: 'componnet', // componnet 10
    auth: 'MODIFY',
    disabled: true
  }
  // {
  //   icon: 'icon-mockjiedian',
  //   name: 'Mock服务',
  //   value: 'serviceMock', // 11
  //   auth: 'VIEW',
  //   disabled: true
  // }
];

export const socketNavs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('service.apis.socketNavs.apiInfo'),
    value: 'apiInfo', // save
    auth: 'MODIFY'
  },
  // {
  //   icon: 'icon-zhibiao',
  //   name: '测试指标',
  //   value: 'perf', // 2
  //   disabled: true,
  //   auth: 'MODIFY'
  // },
  // {
  //   icon: 'icon-zhihangceshi',
  //   name: '测试结果',
  //   value: 'testInfo', // 5
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  {
    icon: 'icon-lishijilu',
    name: t('service.apis.socketNavs.activity'),
    value: 'activity', // 4
    auth: 'VIEW ',
    disabled: true
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: '变量',
  //   value: 'variable', // 6
  //   auth: 'MODIFY',
  //   disabled: true
  // },
  // {
  //   icon: 'icon-fenxiang',
  //   name: '接口分享记录',
  //   value: 'shareList', // 7
  //   auth: 'SHARE',
  //   disabled: true
  // },
  {
    icon: 'icon-jiekoudaili',
    name: t('service.apis.socketNavs.agent'),
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  }
];
