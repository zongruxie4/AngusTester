import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('common.scenario'),
    key: 'scenario'
  },
  {
    title: t('reportAdd.scenarioContent.contentTree.scenarioTotal'),
    key: 'scenarioTotal'

  },
  {
    title: t('reportAdd.scenarioContent.contentTree.scenarioTestDetail'),
    key: 'scenarioTestDetail',
    children: [
      {
        title: t('reportAdd.scenarioContent.contentTree.funcTest'),
        key: 'funcTest',
        children: [
          {
            title: t('reportAdd.scenarioContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('common.api'),
            key: 'apis'
          },
          {
            title: t('reportAdd.scenarioContent.contentTree.apisDetail'),
            key: 'apisDetail'
          }
        ]
      },
      {
        title: t('reportAdd.scenarioContent.contentTree.perfTest'),
        key: 'perfTest',
        children: [
          {
            title: t('reportAdd.scenarioContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('common.detail'),
            key: 'detail'
          },
          {
            title: t('reportAdd.scenarioContent.contentTree.indicator'),
            key: 'indicator'
          }
        ]
      },
      {
        title: t('reportAdd.scenarioContent.contentTree.stabilityTest'),
        key: 'stabilityTest',
        children: [
          {
            title: t('reportAdd.scenarioContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('common.detail'),
            key: 'detail'
          },
          {
            title: t('reportAdd.scenarioContent.contentTree.indicator'),
            key: 'indicator'
          }
        ]
      }
    ]
  }
];
