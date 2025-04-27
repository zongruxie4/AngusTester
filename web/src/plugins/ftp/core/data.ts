export const TOOLBAR_MENUITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'debugResult',
    name: '调试结果'
  },
  {
    key: 'logs',
    name: '调度日志'
  },
  {
    key: 'execLog',
    name: '执行日志'
  }
  // {
  //   key: 'duration',
  //   name: '耗时分析'
  // },
  // {
  //   key: 'cookie',
  //   name: 'Cookie'
  // },
  // {
  //   key: 'assert',
  //   name: '断言结果'
  // },
  // {
  //   key: 'define',
  //   name: '定义'
  // }
];

export const TOOLBAR_EXTRA_MENUITEMS: {
  name:string;
  key:string;
}[] = [
  // {
  //   key: 'status',
  //   name: '状态码'
  // },
  // {
  //   key: 'duration',
  //   name: '耗时'
  // },
  // {
  //   key: 'size',
  //   name: '大小'
  // },
  {
    key: 'toggle',
    name: '展开收起'
  },
  {
    key: 'screen',
    name: '全屏'
  }
];

export const DRAWER_MENUITEMS:{
  name:string;
  key:string;
  icon:string;
}[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: '保存',
    key: 'save'
  },
  {
    icon: 'icon-zhibiao',
    name: '性能指标',
    key: 'indicator'
  },
  {
    icon: 'icon-zhihangceshi',
    key: 'testInfo',
    name: '测试结果'
  },
  // {
  //   icon: 'icon-bianliang',
  //   name: '变量',
  //   key: 'variable'
  // },
  // {
  //   icon: 'icon-canshuhua',
  //   name: '参数化',
  //   key: 'parameterization'
  // },
  {
    icon: 'icon-lishijilu',
    name: '活动',
    key: 'activity'
  },
  {
    icon: 'icon-pinglun',
    name: '评论',
    key: 'comment'
  }
];
