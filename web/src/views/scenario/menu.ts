import { LeftMenuItem } from '@/layout/types';

export enum ScenarioMenuKey {
  HOME = 'home',
  SCENARIO = 'scenario',
  MONITOR = 'monitor',
  TRASH = 'trash'
}

export const createMenuItems = (t: (key: string) => string): LeftMenuItem<ScenarioMenuKey>[] => [
  { icon: 'icon-zhuye', name: t('home.title'), key: ScenarioMenuKey.HOME },
  { icon: 'icon-changjingguanli', name: t('scenario.title'), key: ScenarioMenuKey.SCENARIO },
  { icon: 'icon-jiankong2', name: t('scenarioMonitor.title'), key: ScenarioMenuKey.MONITOR },
  { icon: 'icon-qingchu', name: t('trash.title'), key: ScenarioMenuKey.TRASH }
];
