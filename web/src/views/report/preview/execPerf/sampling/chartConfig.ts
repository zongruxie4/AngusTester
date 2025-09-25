import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((v:string) => v);
export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'tps', 'brps', 'bwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];
export const allCvsNames = {
  duration: t('reportPreview.execPerf.sampling.chartConfig.duration'),
  errors: t('reportPreview.execPerf.sampling.chartConfig.errors'),
  iterations: t('reportPreview.execPerf.sampling.chartConfig.iterations'),
  n: t('reportPreview.execPerf.sampling.chartConfig.n'),
  operations: t('reportPreview.execPerf.sampling.chartConfig.operations'),
  transactions: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
  readBytes: t('reportPreview.execPerf.sampling.chartConfig.readBytes'),
  writeBytes: t('reportPreview.execPerf.sampling.chartConfig.writeBytes'),
  ops: t('reportPreview.execPerf.sampling.chartConfig.ops'),
  tps: t('reportPreview.execPerf.sampling.chartConfig.tps'),
  brps: t('reportPreview.execPerf.sampling.chartConfig.brps'),
  bwps: t('reportPreview.execPerf.sampling.chartConfig.bwps'),
  tranMean: t('reportPreview.execPerf.sampling.chartConfig.tranMean'),
  tranMin: t('reportPreview.execPerf.sampling.chartConfig.tranMin'),
  tranMax: t('reportPreview.execPerf.sampling.chartConfig.tranMax'),
  tranP50: t('reportPreview.execPerf.sampling.chartConfig.tranP50'),
  tranP75: t('reportPreview.execPerf.sampling.chartConfig.tranP75'),
  tranP90: t('reportPreview.execPerf.sampling.chartConfig.tranP90'),
  tranP95: t('reportPreview.execPerf.sampling.chartConfig.tranP95'),
  tranP99: t('reportPreview.execPerf.sampling.chartConfig.tranP99'),
  tranP999: t('reportPreview.execPerf.sampling.chartConfig.tranP999'),
  errorRate: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
  threadPoolSize: t('reportPreview.execPerf.sampling.chartConfig.threadPoolSize'),
  threadPoolActiveSize: t('reportPreview.execPerf.sampling.chartConfig.threadPoolActiveSize'),
  threadMaxPoolSize: t('reportPreview.execPerf.sampling.chartConfig.threadMaxPoolSize')
};

export const allColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    width: '11.5%',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.n'),
    dataIndex: 'n',
    width: '7%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
    dataIndex: 'transactions',
    width: '7%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMean'),
    dataIndex: 'tranMean',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMin'),
    dataIndex: 'tranMin',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMax'),
    dataIndex: 'tranMax',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP50'),
    dataIndex: 'tranP50',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP75'),
    dataIndex: 'tranP75',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP90'),
    dataIndex: 'tranP90',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP99'),
    dataIndex: 'tranP99',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP999'),
    dataIndex: 'tranP999',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.ops'),
    dataIndex: 'ops',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tps'),
    dataIndex: 'tps',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    dataIndex: 'errors',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.brps'),
    dataIndex: 'brps',
    width: '7%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.bwps'),
    dataIndex: 'bwps',
    width: '7%'
  }
];

export const allResponseTimeColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMean'),
    dataIndex: 'tranMean',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMin'),
    dataIndex: 'tranMin',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMax'),
    dataIndex: 'tranMax',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP50'),
    dataIndex: 'tranP50',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP75'),
    dataIndex: 'tranP75',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP90'),
    dataIndex: 'tranP90',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP95'),
    dataIndex: 'tranP95',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP99'),
    dataIndex: 'tranP99',
    width: '9.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP999'),
    dataIndex: 'tranP999',
    width: '9.5%'
  }
];

export const allErrorsColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    dataIndex: 'errors',
    width: '66%'
  }
];

export const allErrorRateColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '66%'
  }
];

export const allUploadColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.brps'),
    dataIndex: 'brps',
    width: '33%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.bwps'),
    dataIndex: 'bwps',
    width: '33%'
  }
];

export const allRowsColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.iterations'),
    dataIndex: 'iterations',
    width: '25%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
    dataIndex: 'transactions',
    width: '25%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    dataIndex: 'errors',
    width: '25%'
  }
];

export const throughputCvsKeys = ['ops', 'tps', 'brps', 'bwps'];

export const throughputOptions = [
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.ops'),
    value: 'ops'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tps'),
    value: 'tps'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.brps'),
    value: 'brps'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.bwps'),
    value: 'bwps'
  }
];

export const throughputColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.ops'),
    dataIndex: 'ops',
    width: '16.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tps'),
    dataIndex: 'tps',
    width: '16.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.brps'),
    dataIndex: 'brps',
    width: '16.5%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.bwps'),
    dataIndex: 'bwps',
    width: '16.5%'
  }
];

export const threadCvsKeys = ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const threadOptions = [
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.threadPoolSize'),
    value: 'threadPoolSize'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.threadPoolActiveSize'),
    value: 'threadPoolActiveSize'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.threadMaxPoolSize'),
    value: 'threadMaxPoolSize'
  }
];

export const threadColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.threadPoolSize'),
    dataIndex: 'threadPoolSize',
    width: '22%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.threadPoolActiveSize'),
    dataIndex: 'threadPoolActiveSize',
    width: '22%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.threadMaxPoolSize'),
    dataIndex: 'threadMaxPoolSize',
    width: '22%'
  }
];

export const responseTimeCvsKeys = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];

export const responseTimeOptions = [
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranMean'),
    value: 'tranMean'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranMin'),
    value: 'tranMin'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranMax'),
    value: 'tranMax'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP50'),
    value: 'tranP50'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP75'),
    value: 'tranP75'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP90'),
    value: 'tranP90'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP95'),
    value: 'tranP95'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP99'),
    value: 'tranP99'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.tranP999'),
    value: 'tranP999'
  }
];

export const responseTimeColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMean'),
    dataIndex: 'tranMean',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMin'),
    dataIndex: 'tranMin',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranMax'),
    dataIndex: 'tranMax',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP50'),
    dataIndex: 'tranP50',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP75'),
    dataIndex: 'tranP75',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP90'),
    dataIndex: 'tranP90',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP95'),
    dataIndex: 'tranP95',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP99'),
    dataIndex: 'tranP99',
    width: '8%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.tranP999'),
    dataIndex: 'tranP999',
    width: '8%'
  }
];

export const errorCvsKeys = ['errors', 'n', 'operations', 'transactions', 'errorRate'];

export const errorOptions = [
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.n'),
    value: 'n'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.operations'),
    value: 'operations'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
    value: 'transactions'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    value: 'errors'
  },
  {
    label: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
    value: 'errorRate'
  }
];
export const errorColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.n'),
    dataIndex: 'n',
    width: '12%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.operations'),
    dataIndex: 'operations',
    width: '12%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
    dataIndex: 'transactions',
    width: '12%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    dataIndex: 'errors',
    width: '12%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '12%'
  }
];

export const oneApiErrorColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.n'),
    dataIndex: 'n',
    width: '15%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.transactions'),
    dataIndex: 'transactions',
    width: '15%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errors'),
    dataIndex: 'errors',
    width: '15%'
  },
  {
    title: t('reportPreview.execPerf.sampling.chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '15%'
  }
];
