import { LeftMenuItem } from '@/layout/types';

export enum ApiMenuKey {
  HOME = 'home',
  SERVICES = 'services',
  MOCK = 'mock',
  DESIGN = 'design',
  SHARE = 'share',
  SERVER = 'server',
  TRASH = 'trash'
}

export const createMenuItems = (t: (key: string) => string): LeftMenuItem<ApiMenuKey>[] => [
  { name: t('home.title'), icon: 'icon-zhuye', key: ApiMenuKey.HOME },
  { name: t('service.title'), icon: 'icon-fuwuxinxi', key: ApiMenuKey.SERVICES },
  { name: t('mock.title'), icon: 'icon-fuwuxinxi', key: ApiMenuKey.MOCK },
  { name: t('apiDesign.title'), icon: 'icon-sheji', key: ApiMenuKey.DESIGN },
  { name: t('apiShare.title'), icon: 'icon-fenxiang', key: ApiMenuKey.SHARE },
  { name: t('apiServer.title'), icon: 'icon-host', key: ApiMenuKey.SERVER },
  { name: t('trash.title'), icon: 'icon-qingchu', key: ApiMenuKey.TRASH }
];
