import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

export const TOOLBAR_MENUITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'debugResult',
    name: t('common.debugResult')
  },
  {
    key: 'logs',
    name: t('common.scheduleLog')
  },
  {
    key: 'execLog',
    name: t('common.execLog')
  }
];

export const TOOLBAR_EXTRA_MENUITEMS: {
  name:string;
  key:string;
}[] = [

  {
    key: 'toggle',
    name: t('actions.toggle')
  },
  {
    key: 'screen',
    name: t('actions.fullScreen')
  }
];

export const DRAWER_MENUITEMS:{
  name:string;
  key:string;
  icon:string;
}[] = [
  {
    icon: 'icon-fuwuxinxi',
    name: t('commonPlugin.menuData.drawer.save'),
    key: 'save'
  },
  {
    icon: 'icon-zhibiao',
    name: t('commonPlugin.menuData.drawer.indicator'),
    key: 'indicator'
  },
  {
    icon: 'icon-zhihangceshi',
    key: 'testInfo',
    name: t('commonPlugin.menuData.drawer.testInfo')
  },
  {
    icon: 'icon-lishijilu',
    name: t('commonPlugin.menuData.drawer.activity'),
    key: 'activity'
  },
  {
    icon: 'icon-pinglun',
    name: t('common.comment'),
    key: 'comment'
  }
];
