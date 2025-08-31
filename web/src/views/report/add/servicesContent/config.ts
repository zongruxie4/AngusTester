import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.servicesContent.contentTree.serviceDetail'),
    key: 'serviceDetail'
  },
  {
    title: t('reportAdd.servicesContent.contentTree.apisGroupedTotal'),
    key: 'apisGroupedTotal'
  },
  {
    title: t('reportAdd.servicesContent.contentTree.serviceTotal'),
    key: 'serviceTotal',
    children: [
      {
        title: t('reportAdd.servicesContent.contentTree.testProcess'),
        key: 'testProcess'
      },
      {
        title: t('reportAdd.servicesContent.contentTree.testApi'),
        key: 'testApi'
      },
      {
        title: t('reportAdd.servicesContent.contentTree.testStatus'),
        key: 'testStatus'
      }
    ]
  }
];
