import { Method } from '@/views/apis/services/apiHttp/interface';

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
  favouriteFlag: boolean,
  id: string,
  show: boolean,
  authFlag:boolean,
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
  serviceAuthFlag: boolean
}
export const navs: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: '接口信息',
    value: 'apiInfo', // 1
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhibiao',
    name: '测试指标',
    value: 'performance', // 2
    disabled: true,
    auth: 'MODIFY'
  },
  {
    icon: 'icon-zhihangceshi',
    name: '测试结果',
    value: 'testInfo', // 5
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-yongliku',
    name: '接口用例',
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
    name: '活动',
    value: 'activity', // 4
    auth: 'VIEW',
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
    name: '接口代理',
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-daimashitu',
    name: '代码',
    value: 'code',
    auth: 'VIEW',
    disabled: true
  },
  {
    icon: 'icon-mockjiedian',
    name: 'Mock接口',
    value: 'apiMock',
    auth: 'VIEW',
    disabled: true
  }
];

export const serviceNav: NavItem[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: '服务信息',
    value: 'projectInfo', // 1
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-wendangxinxi',
    name: '文档信息',
    value: 'openapi',
    auth: 'GRANT',
    disabled: true
  },
  {
    icon: 'icon-peizhifuwutongbu',
    name: '同步配置',
    value: 'syncConfig', // 6
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-renzhengtou',
    name: '安全方案配置',
    value: 'security', // 7
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-host',
    name: '服务器配置',
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
    name: '活动',
    auth: 'VIEW',
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
    name: '服务代理',
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-biaoqian',
    name: '标签',
    value: 'tag', // 9
    auth: 'MODIFY',
    disabled: true
  },
  {
    icon: 'icon-zujian',
    name: '组件',
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
    name: '接口信息',
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
    name: '活动',
    value: 'activity', // 4
    auth: 'VIEW',
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
    name: '接口代理',
    value: 'agent',
    auth: 'MODIFY',
    disabled: true
  }
];
