import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.projectProcessContent.contentTree.projectInfo'),
    key: 'projectInfo'
  },
  {
    title: t('common.issue'),
    key: 'task',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.taskTotal'),
        key: 'taskTotal'
      },
      {
        title: t('chart.burndown.countBurndown'),
        key: 'taskBurndown'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.taskGroup'),
        key: 'taskGroup'
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
        key: 'caseBurndown'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.caseGroup'),
        key: 'caseGroup'
      }
    ]
  },
  {
    title: t('common.api'),
    key: 'apis',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.apisTotal'),
        key: 'apisTotal'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.apisGroup'),
        key: 'apisGroup'
      }
    ]
  },
  {
    title: t('common.scenario'),
    key: 'scenario',
    children: [
      {
        title: t('reportAdd.projectProcessContent.contentTree.scenarioTotal'),
        key: 'scenarioTotal'
      },
      {
        title: t('reportAdd.projectProcessContent.contentTree.scenarioGroup'),
        key: 'scenarioGroup'
      }
    ]
  }
];
