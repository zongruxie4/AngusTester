import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.apisContent.contentTree.apisBasic'),
    key: 'apisBasic'
  },
  {
    title: t('reportAdd.apisContent.contentTree.apisTest'),
    key: 'apisTest'
  },
  {
    title: t('reportAdd.apisContent.contentTree.apisDetail'),
    key: 'apisDetail',
    children: [
      {
        title: t('reportAdd.apisContent.contentTree.funcTest'),
        key: 'funcTest',
        children: [
          {
            title: t('reportAdd.apisContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('reportAdd.apisContent.contentTree.cases'),
            key: 'cases'
          },
          {
            title: t('reportAdd.apisContent.contentTree.casesDetail'),
            key: 'casesDetail'
          }
        ]
      },
      {
        title: t('reportAdd.apisContent.contentTree.perfTest'),
        key: 'perfTest',
        children: [
          {
            title: t('reportAdd.apisContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('reportAdd.apisContent.contentTree.detail'),
            key: 'detail'
          },
          {
            title: t('reportAdd.apisContent.contentTree.indicator'),
            key: 'indicator'
          }
        ]
      },
      {
        title: t('reportAdd.apisContent.contentTree.stabilityTest'),
        key: 'stabilityTest',
        children: [
          {
            title: t('reportAdd.apisContent.contentTree.info'),
            key: 'info'
          },
          {
            title: t('reportAdd.apisContent.contentTree.detail'),
            key: 'detail'
          },
          {
            title: t('reportAdd.apisContent.contentTree.indicator'),
            key: 'indicator'
          }
        ]
      }
    ]
  }
];
