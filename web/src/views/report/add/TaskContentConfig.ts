import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.taskContent.contentTree.basic'),
    key: 'basic'
  },
  {
    title: t('reportAdd.taskContent.contentTree.user'),
    key: 'user'
  },
  {
    title: t('reportAdd.taskContent.contentTree.date'),
    key: 'date'
  },
  {
    title: t('reportAdd.taskContent.contentTree.description'),
    key: 'description'
  },
  {
    title: t('reportAdd.taskContent.contentTree.subTask'),
    key: 'subTask'
  },
  {
    title: t('reportAdd.taskContent.contentTree.task'),
    key: 'task'
  },
  {
    title: t('reportAdd.taskContent.contentTree.cases'),
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
        title: t('reportAdd.taskContent.contentTree.exec'),
        key: 'exec'
      },
      {
        title: t('reportAdd.taskContent.contentTree.result'),
        key: 'result'
      }
    ]
  },
  {
    title: t('reportAdd.taskContent.contentTree.remark'),
    key: 'remark'
  },
  {
    title: t('reportAdd.taskContent.contentTree.activity'),
    key: 'activity'
  },
  {
    title: t('reportAdd.taskContent.contentTree.comment'),
    key: 'comment'
  }
];
