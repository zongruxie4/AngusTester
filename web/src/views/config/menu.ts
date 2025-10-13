import { LeftMenuItem } from '@/layout/types';

export enum ConfigMenuKey {
  APP_INFO = 'appInfo',
  INDICATOR = 'indicator',
  EVENT = 'event',
  NODE = 'node',
  PROXY = 'proxy'
}

export const createMenuItems = (t: (key: string) => string): LeftMenuItem<ConfigMenuKey>[] => [
  { icon: 'icon-yingyongxinxi', name: t('app.title'), key: ConfigMenuKey.APP_INFO },
  { icon: 'icon-zhibiao', name: t('indicator.title'), key: ConfigMenuKey.INDICATOR },
  { icon: 'icon-tuisongtongzhi', name: t('event.title'), key: ConfigMenuKey.EVENT },
  { icon: 'icon-guanlijiedian', name: t('node.title'), key: ConfigMenuKey.NODE },
  { icon: 'icon-jiekoudaili', name: t('proxy.title'), key: ConfigMenuKey.PROXY }
];
