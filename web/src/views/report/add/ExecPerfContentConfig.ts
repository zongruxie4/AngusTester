import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const contentTreeData = [
  {
    title: t('common.execution'),
    key: 'exec',
    children: [
      {
        title: t('reportAdd.execPerfContent.contentTree.basic'),
        key: 'basic'
      },
      {
        title: t('common.execResult'),
        key: 'execResult'
      }
    ]
  },
  {
    title: t('chart.total'),
    key: 'total',
    children: [
      {
        title: t('common.name'),
        key: 'name'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.sampling'),
        key: 'sampling'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.qps'),
        key: 'qps'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.minmax'),
        key: 'minmax'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.qps/s'),
        key: 'qps/s'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.errRate0'),
        key: 'errRate0'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.percent'),
        key: 'percent'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.download/s0'),
        key: 'download/s0'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.upload/s0'),
        key: 'upload/s0'
      }
    ]
  },
  {
    title: t('reportAdd.execPerfContent.contentTree.indicatorChart'),
    key: 'indicatorChart',
    children: [
      {
        title: t('reportAdd.execPerfContent.contentTree.tps'),
        key: 'tps'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.thread0'),
        key: 'thread0'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.response'),
        key: 'response'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.error0'),
        key: 'error0'
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.statusCode'),
        key: 'statusCode'
      },
      {
        title: t('common.node'),
        key: 'node',
        tips: t('reportAdd.execPerfContent.contentTree.nodeTips')
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.superposition'),
        key: 'superposition',
        children: [
          {
            title: t('reportAdd.execPerfContent.contentTree.QPS/TPS'),
            key: 'QPS/TPS',
            children: [
              {
                title: t('reportAdd.execPerfContent.contentTree.QPS/s'),
                key: 'QPS/s'
              }
            ]
          },
          {
            title: t('reportAdd.execPerfContent.contentTree.vu'),
            key: 'vu',
            children: [
              {
                title: t('reportAdd.execPerfContent.contentTree.thread'),
                key: 'thread'
              }
            ]
          },
          {
            title: t('reportAdd.execPerfContent.contentTree.rt'),
            key: 'rt',
            children: [
              {
                title: t('chart.average'),
                key: 'average'
              }
            ]
          },
          {
            title: t('reportAdd.execPerfContent.contentTree.error'),
            key: 'error',
            children: [
              {
                title: t('reportAdd.execPerfContent.contentTree.errRate'),
                key: 'errRate'
              }
            ]
          }
        ]
      }
    ]
  },
  {
    title: t('reportAdd.execPerfContent.contentTree.other'),
    key: 'other',
    children: [
      {
        title: t('reportAdd.execPerfContent.contentTree.dispatch'),
        key: 'dispatch',
        tips: t('reportAdd.execPerfContent.contentTree.dispatchTips')
      },
      {
        title: t('reportAdd.execPerfContent.contentTree.samplingLog'),
        key: 'sampling',
        tips: t('reportAdd.execPerfContent.contentTree.samplingTips')
      }
    ]
  }
];
