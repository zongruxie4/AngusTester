import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.caseContent.contentTree.basic'),
    key: 'basic'
  },
  {
    title: t('organization.user'),
    key: 'user'
  },
  {
    title: t('reportAdd.caseContent.contentTree.date'),
    key: 'date'
  },
  {
    title: t('reportAdd.caseContent.contentTree.condition'),
    key: 'condition'
  },
  {
    title: t('reportAdd.caseContent.contentTree.steps'),
    key: 'steps'
  },
  {
    title: t('common.description'),
    key: 'description'
  },
  {
    title: t('actions.preview'),
    key: 'preview'
  },
  {
    title: t('reportAdd.caseContent.contentTree.test'),
    key: 'test'
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
    title: t('reportAdd.caseContent.contentTree.activity'),
    key: 'activity'
  },
  {
    title: t('common.comment'),
    key: 'comment'
  }
];
