import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportAdd.planContent.contentTree.planDetail'),
    key: 'planDetail',
    children: [
      {
        title: t('common.basicInfo'),
        key: 'basic'
      },
      {
        title: t('common.tester'),
        key: 'tester'
      },
      {
        title: t('reportAdd.planContent.contentTree.objective'),
        key: 'objective'
      },
      {
        title: t('common.scope'),
        key: 'scope'
      },
      {
        title: t('reportAdd.planContent.contentTree.acceptanceCriteria'),
        key: 'acceptanceCriteria'
      },
      {
        title: t('reportAdd.planContent.contentTree.other'),
        key: 'other'
      }
    ]
  },
  {
    title: t('reportAdd.planContent.contentTree.caseTotal'),
    key: 'caseTotal',
    children: [
      {
        title: t('reportAdd.planContent.contentTree.casesTotal'),
        key: 'casesTotal'
      },
      {
        title: t('reportAdd.planContent.contentTree.casesReviewTotal'),
        key: 'casesReviewTotal'
      },
      {
        title: t('reportAdd.planContent.contentTree.burndown'),
        key: 'burndown'
      },
      {
        title: t('reportAdd.planContent.contentTree.grouped'),
        key: 'grouped'
      }
    ]
  },
  {
    title: t('reportAdd.planContent.contentTree.resultDetail'),
    key: 'resultDetail',
    children: [
      {
        title: t('reportAdd.planContent.contentTree.testTotal'),
        key: 'testTotal'
      },
      {
        title: t('reportAdd.planContent.contentTree.reviewTotal'),
        key: 'reviewTotal'
      },
      {
        title: t('chart.burndown.title'),
        key: 'burndown'
      },
      {
        title: t('reportAdd.planContent.contentTree.groupedTotal'),
        key: 'groupedTotal'
      }
    ]
  }
];
