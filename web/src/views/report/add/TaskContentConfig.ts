import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.taskContent.contentTree.basic'),
    key: 'basic'
  },
  {
    title: t('organization.user'),
    key: 'user'
  },
  {
    title: t('reportAdd.taskContent.contentTree.date'),
    key: 'date'
  },
  {
    title: t('common.description'),
    key: 'description'
  },
  {
    title: t('reportAdd.taskContent.contentTree.subTask'),
    key: 'subTask'
  },
  {
    title: t('common.issue'),
    key: 'task'
  },
  {
    title: t('common.useCase'),
    key: 'cases'
  },
  {
    title: t('reportAdd.taskContent.contentTree.test'),
    key: 'test',
    tips: t('reportAdd.taskContent.contentTree.testTips'),
    children: [
      {
        title: t('reportAdd.taskContent.contentTree.testSource'),
        key: 'testSource'
      },
      {
        title: t('common.execution'),
        key: 'exec'
      },
      {
        title: t('reportAdd.taskContent.contentTree.result'),
        key: 'result'
      }
    ]
  },
  {
    title: t('common.remark'),
    key: 'remark'
  },
  {
    title: t('reportAdd.taskContent.contentTree.activity'),
    key: 'activity'
  },
  {
    title: t('common.comment'),
    key: 'comment'
  }
];
