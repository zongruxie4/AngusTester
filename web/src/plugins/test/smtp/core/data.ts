import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

export const TOOLBAR_MENUITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'debugResult',
    name: t('commonPlugin.menuData.toolbar.debugResult')
  },
  {
    key: 'logs',
    name: t('commonPlugin.menuData.toolbar.logs')
  },
  {
    key: 'execLog',
    name: t('commonPlugin.menuData.toolbar.execLog')
  }

];

export const TOOLBAR_EXTRA_MENUITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'duration',
    name: t('commonPlugin.menuData.toolbar.duration')
  },
  {
    key: 'size',
    name: t('common.size')
  },
  {
    key: 'toggle',
    name: t('commonPlugin.menuData.toolbar.toggle')
  },
  {
    key: 'screen',
    name: t('commonPlugin.menuData.toolbar.screen')
  }
];

export const DRAWER_MENUITEMS:{
  name:string;
  key:string;
  icon:string;
}[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('actions.save'),
    key: 'save'
  },
  // {
  //   icon: 'icon-zhibiao',
  //   name: '性能指标',
  //   key: 'indicator'
  // },
  // {
  //   icon: 'icon-zhihangceshi',
  //   key: 'testInfo',
  //   name: '测试结果'
  // },
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
    name: t('commonPlugin.menuData.drawer.activity'),
    key: 'activity'
  },
  {
    icon: 'icon-pinglun',
    name: t('commonPlugin.menuData.drawer.comment'),
    key: 'comment'
  }
];
