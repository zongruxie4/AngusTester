import { LeftMenuItem } from '@/layout/types';

export enum DataMenuKey {
  HOME = 'home',
  VARIABLES = 'variables',
  DATASET = 'dataset',
  FILE = 'file',
  DATASOURCE = 'datasource'
}

export const createMenuItems = (t: (key: string) => string): LeftMenuItem<DataMenuKey>[] => [
  { name: t('home.title'), icon: 'icon-zhuye', key: DataMenuKey.HOME },
  { name: t('variable.title'), icon: 'icon-bianliang1', key: DataMenuKey.VARIABLES },
  { name: t('dataset.title'), icon: 'icon-shujuji', key: DataMenuKey.DATASET },
  { name: t('file.title'), icon: 'icon-wenjian1', key: DataMenuKey.FILE },
  { name: t('datasource.title'), icon: 'icon-shujuyuan', key: DataMenuKey.DATASOURCE }
];
