import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('reportPreview.evaluation.contentTree.evaluationInfo'),
    key: 'evaluationInfo',
    children: [
      {
        title: t('reportPreview.evaluation.contentTree.indicator'),
        key: 'indicator'
      },
      {
        title: t('reportPreview.evaluation.contentTree.result'),
        key: 'result'
      }
    ]
  },
  {
    title: t('reportPreview.evaluation.contentTree.case'),
    key: 'case'
  },
];
