import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.projectProcessContent.contentTree.projectInfo'),
    key: 'projectInfo'
  },
  {
    title: t('reportAdd.projectProcessContent.contentTree.task'),
    key: 'task',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.taskTotal'),
        key: 'taskTotal'
      },
      {
        title: t('chart.burndown.countBurndown'),
        key: 'taskburndown'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.taskgroup'),
        key: 'taskgroup'
      }
    ]
  },
  {
    title: t('reportAdd.projectProcessContent.contentTree.func'),
    key: 'func',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.funcTotal'),
        key: 'funcTotal'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.funcReviewTotal'),
        key: 'funcReviewTotal'
      },
      {
        title: t('chart.burndown.countBurndown'),
        key: 'caseburndown'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.casegroup'),
        key: 'casegroup'
      }
    ]
  },
  {
    title: t('reportAdd.projectProcessContent.contentTree.apis'),
    key: 'apis',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.apisTotal'),
        key: 'apisTotal'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.apisgroup'),
        key: 'apisgroup'
      }
    ]
  },
  {
    title: t('reportAdd.projectProcessContent.contentTree.scenario'),
    key: 'scenario',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.scenarioTotal'),
        key: 'scenarioTotal'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.scenariogroup'),
        key: 'scenariogroup'
      }
    ]
  }
];
