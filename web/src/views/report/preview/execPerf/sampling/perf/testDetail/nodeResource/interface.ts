import { i18n } from '@xcan-angus/infra';
const t = i18n.getI18n()?.global?.t || ((val: string) => val);

export const columns = [
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.name'),
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.average'),
    dataIndex: 'average',
    key: 'average'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.high'),
    dataIndex: 'high',
    key: 'high'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.low'),
    dataIndex: 'low',
    key: 'low'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.latest'),
    dataIndex: 'latest',
    key: 'latest'
  }
];
