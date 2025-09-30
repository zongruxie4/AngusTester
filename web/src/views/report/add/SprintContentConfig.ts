import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.sprintContent.contentTree.sprintDetail'),
    key: 'sprintDetail',
    children: [
      {
        title: t('reportAdd.sprintContent.contentTree.basic'),
        key: 'basic'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.taskDetail'),
        key: 'taskDetail'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.meetings'),
        key: 'meetings'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.others'),
        key: 'others'
      }
    ]
  },
  {
    title: t('common.issue'),
    key: 'task',
    children: [
      {
        title: t('reportAdd.sprintContent.contentTree.taskTotal'),
        key: 'taskTotal'
      },
      {
        title: t('chart.burndown.countBurndown'),
        key: 'taskBurndown'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.groupedTotal'),
        key: 'groupedTotal'
      }
    ]
  },
  {
    title: t('reportAdd.sprintContent.contentTree.assigneeId'),
    key: 'assigneeId',
    children: [
      {
        title: t('reportAdd.sprintContent.contentTree.assigneeId_taskTotal'),
        key: 'assigneeId_taskTotal'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.assigneeId_burndown'),
        key: 'assigneeId_burndown'
      },
      {
        title: t('reportAdd.sprintContent.contentTree.assigneeId_groupedTotal'),
        key: 'assigneeId_groupedTotal'
      }
    ]
  }
];
