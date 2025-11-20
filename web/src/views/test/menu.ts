import { LeftMenuItem } from '@/layout/types';
import { EditionType } from '@xcan-angus/infra';

export enum TestMenuKey {
  HOME = 'home',
  PLANS = 'plans',
  CASES = 'cases',
  REVIEWS = 'reviews',
  BASELINE = 'baseline',
  ANALYSIS = 'analysis',
  TEMPLATES = 'templates',
  TRASH = 'trash'
}

export const createMenuItems = (
  t: (key: string) => string,
  editionType: EditionType | undefined
): LeftMenuItem<TestMenuKey>[] => {
  const items: Array<LeftMenuItem<TestMenuKey> | null> = [
    {
      name: t('home.title'),
      key: TestMenuKey.HOME,
      icon: 'icon-zhuye'
    },
    {
      name: t('testPlan.title'),
      key: TestMenuKey.PLANS,
      icon: 'icon-jihua1'
    },
    {
      name: t('testCase.title'),
      key: TestMenuKey.CASES,
      icon: 'icon-ceshiyongli1'
    },
    {
      name: t('testCaseReview.title'),
      key: TestMenuKey.REVIEWS,
      icon: 'icon-pingshen'
    },
    {
      name: t('testCaseBaseline.title'),
      key: TestMenuKey.BASELINE,
      icon: 'icon-jixian'
    },
    editionType !== undefined && editionType !== EditionType.COMMUNITY
      ? {
          name: t('testAnalysis.title'),
          key: TestMenuKey.ANALYSIS,
          icon: 'icon-fenxi'
        }
      : null,
    {
      name: t('testTemplate.title'),
      key: TestMenuKey.TEMPLATES,
      icon: 'icon-kanbanshitu'
    },
    {
      name: t('trash.title'),
      key: TestMenuKey.TRASH,
      icon: 'icon-qingchu'
    }
  ];
  return items.filter((i): i is LeftMenuItem<TestMenuKey> => i !== null);
};
