import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('common.execution'),
    key: 'exec',
    children: [
      {
        title: t('reportAdd.execFuncContent.contentTree.basic'),
        key: 'basic'
      },
      {
        title: t('common.execResult'),
        key: 'execResult'
      }
    ]
  },
  {
    title: t('common.sprint'),
    key: 'sprint'
  },
  {
    title: t('reportAdd.execFuncContent.contentTree.log'),
    key: 'log',
    children: [
      {
        title: t('reportAdd.execFuncContent.contentTree.dispatch'),
        key: 'dispatch',
        tips: t('reportAdd.execFuncContent.contentTree.dispatchTips')
      },
      {
        title: t('reportAdd.execFuncContent.contentTree.sampling'),
        key: 'sampling',
        tips: t('reportAdd.execFuncContent.contentTree.samplingTips')
      }
    ]
  }
];
