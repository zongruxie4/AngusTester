export const TOOLBAR_MENU_ITEMS: {
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
  ];

export const TOOLBAR_EXTRA_MENU_ITEMS: {
    name:string;
    key:string;
  }[] = [
    {
      key: 'toggle',
      name: '展开收起'
    },
    {
      key: 'screen',
      name: '全屏'
    }
  ];

// 脚本语言
export const LANG_OPTIONS = [
  {
    value: 'yaml',
    label: 'YAML'
  },
  {
    value: 'json',
    label: 'JSON'
  }
];
