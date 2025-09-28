import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'tps', 'brps', 'bwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const allCvsNames = {
  duration: t('execution.chartConfig.names.duration'),
  errors: t('execution.chartConfig.names.errors'),
  iterations: t('execution.chartConfig.names.iterations'),
  n: t('execution.chartConfig.names.n'),
  operations: t('execution.chartConfig.names.operations'),
  transactions: t('execution.chartConfig.names.transactions'),
  readBytes: t('execution.chartConfig.names.readBytes'),
  writeBytes: t('execution.chartConfig.names.writeBytes'),
  ops: t('execution.chartConfig.names.ops'),
  tps: t('execution.chartConfig.names.tps'),
  brps: t('execution.chartConfig.names.brps'),
  bwps: t('execution.chartConfig.names.bwps'),
  tranMean: t('execution.chartConfig.names.tranMean'),
  tranMin: t('execution.chartConfig.names.tranMin'),
  tranMax: t('execution.chartConfig.names.tranMax'),
  tranP50: t('execution.chartConfig.names.tranP50'),
  tranP75: t('execution.chartConfig.names.tranP75'),
  tranP90: t('execution.chartConfig.names.tranP90'),
  tranP95: t('execution.chartConfig.names.tranP95'),
  tranP99: t('execution.chartConfig.names.tranP99'),
  tranP999: t('execution.chartConfig.names.tranP999'),
  errorRate: t('execution.chartConfig.names.errorRate'),
  threadPoolSize: t('execution.chartConfig.names.threadPoolSize'),
  threadPoolActiveSize: t('execution.chartConfig.names.threadPoolActiveSize'),
  threadMaxPoolSize: t('execution.chartConfig.names.threadMaxPoolSize')
};

export const allColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    width: '11.5%',
    ellipsis: true
  },
  {
    title: t('execution.chartConfig.columns.samplingCount'),
    dataIndex: 'n',
    width: '7%'
  },
  {
    title: t('execution.chartConfig.columns.transactionCount'),
    dataIndex: 'transactions',
    width: '7%'
  },
  {
    title: t('execution.chartConfig.columns.average'),
    dataIndex: 'tranMean',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.minimum'),
    dataIndex: 'tranMin',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.maximum'),
    dataIndex: 'tranMax',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.p50'),
    dataIndex: 'tranP50',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.p75'),
    dataIndex: 'tranP75',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.p90'),
    dataIndex: 'tranP90',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.p99'),
    dataIndex: 'tranP99',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.p999'),
    dataIndex: 'tranP999',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.queriesPerSecond'),
    dataIndex: 'ops',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.transactionsPerSecond'),
    dataIndex: 'tps',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.errorCount'),
    dataIndex: 'errors',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.errorRate'),
    dataIndex: 'errorRate',
    width: '5%'
  },
  {
    title: t('execution.chartConfig.columns.downloadPerSecond'),
    dataIndex: 'brps',
    width: '7%'
  },
  {
    title: t('execution.chartConfig.columns.uploadPerSecond'),
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
    title: t('execution.chartConfig.columns.average'),
    dataIndex: 'tranMean',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.minimum'),
    dataIndex: 'tranMin',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.maximum'),
    dataIndex: 'tranMax',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p50'),
    dataIndex: 'tranP50',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p75'),
    dataIndex: 'tranP75',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p90'),
    dataIndex: 'tranP90',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p95'),
    dataIndex: 'tranP95',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p99'),
    dataIndex: 'tranP99',
    width: '9.5%'
  },
  {
    title: t('execution.chartConfig.columns.p999'),
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
    title: t('execution.chartConfig.columns.errorCount'),
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
    title: t('execution.chartConfig.columns.errorRate'),
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
    title: t('execution.chartConfig.columns.downloadPerSecond'),
    dataIndex: 'brps',
    width: '33%'
  },
  {
    title: t('execution.chartConfig.columns.uploadPerSecond'),
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
    title: t('execution.chartConfig.columns.iterationCount'),
    dataIndex: 'iterations',
    width: '25%'
  },
  {
    title: t('execution.chartConfig.columns.transactionCount'),
    dataIndex: 'transactions',
    width: '25%'
  },
  {
    title: t('execution.chartConfig.columns.errorCount'),
    dataIndex: 'errors',
    width: '25%'
  }
];

export const throughputCvsKeys = ['ops', 'tps', 'brps', 'bwps'];

export const throughputOptions = [
  {
    label: t('execution.chartConfig.options.queriesPerSecond'),
    value: 'ops'
  },
  {
    label: t('execution.chartConfig.options.transactionsPerSecond'),
    value: 'tps'
  },
  {
    label: t('execution.chartConfig.options.downloadPerSecond'),
    value: 'brps'
  },
  {
    label: t('execution.chartConfig.options.uploadPerSecond'),
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
    title: t('execution.chartConfig.columns.queriesPerSecond'),
    dataIndex: 'ops',
    width: '16.5%'
  },
  {
    title: t('execution.chartConfig.columns.transactionsPerSecond'),
    dataIndex: 'tps',
    width: '16.5%'
  },
  {
    title: t('execution.chartConfig.columns.downloadPerSecond'),
    dataIndex: 'brps',
    width: '16.5%'
  },
  {
    title: t('execution.chartConfig.columns.uploadPerSecond'),
    dataIndex: 'bwps',
    width: '16.5%'
  }
];

export const threadCvsKeys = ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const threadOptions = [
  {
    label: t('execution.chartConfig.options.threadCount'),
    value: 'threadPoolSize'
  },
  {
    label: t('execution.chartConfig.options.activeThreadCount'),
    value: 'threadPoolActiveSize'
  },
  {
    label: t('execution.chartConfig.options.maxThreadCount'),
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
    title: t('execution.chartConfig.columns.threadCount'),
    dataIndex: 'threadPoolSize',
    width: '22%'
  },
  {
    title: t('execution.chartConfig.columns.activeThreadCount'),
    dataIndex: 'threadPoolActiveSize',
    width: '22%'
  },
  {
    title: t('execution.chartConfig.columns.maxThreadCount'),
    dataIndex: 'threadMaxPoolSize',
    width: '22%'
  }
];

export const responseTimeCvsKeys = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];

export const responseTimeOptions = [
  {
    label: t('execution.chartConfig.options.average'),
    value: 'tranMean'
  },
  {
    label: t('execution.chartConfig.options.minimum'),
    value: 'tranMin'
  },
  {
    label: t('execution.chartConfig.options.maximum'),
    value: 'tranMax'
  },
  {
    label: t('execution.chartConfig.options.p50'),
    value: 'tranP50'
  },
  {
    label: t('execution.chartConfig.options.p75'),
    value: 'tranP75'
  },
  {
    label: t('execution.chartConfig.options.p90'),
    value: 'tranP90'
  },
  {
    label: t('execution.chartConfig.options.p95'),
    value: 'tranP95'
  },
  {
    label: t('execution.chartConfig.options.p99'),
    value: 'tranP99'
  },
  {
    label: t('execution.chartConfig.options.p999'),
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
    title: t('execution.chartConfig.columns.average'),
    dataIndex: 'tranMean',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.minimum'),
    dataIndex: 'tranMin',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.maximum'),
    dataIndex: 'tranMax',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p50'),
    dataIndex: 'tranP50',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p75'),
    dataIndex: 'tranP75',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p90'),
    dataIndex: 'tranP90',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p95'),
    dataIndex: 'tranP95',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p99'),
    dataIndex: 'tranP99',
    width: '8%'
  },
  {
    title: t('execution.chartConfig.columns.p999'),
    dataIndex: 'tranP999',
    width: '8%'
  }
];

export const errorCvsKeys = ['errors', 'n', 'operations', 'transactions', 'errorRate'];

export const errorOptions = [
  {
    label: t('execution.chartConfig.options.samplingCount'),
    value: 'n'
  },
  {
    label: t('execution.chartConfig.options.requestCount'),
    value: 'operations'
  },
  {
    label: t('execution.chartConfig.options.transactionCount'),
    value: 'transactions'
  },
  {
    label: t('execution.chartConfig.options.errorCount'),
    value: 'errors'
  },
  {
    label: t('execution.chartConfig.options.errorRate'),
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
    title: t('execution.chartConfig.columns.samplingCount'),
    dataIndex: 'n',
    width: '12%'
  },
  {
    title: t('execution.chartConfig.columns.requestCount'),
    dataIndex: 'operations',
    width: '12%'
  },
  {
    title: t('execution.chartConfig.columns.transactionCount'),
    dataIndex: 'transactions',
    width: '12%'
  },
  {
    title: t('execution.chartConfig.columns.errorCount'),
    dataIndex: 'errors',
    width: '12%'
  },
  {
    title: t('execution.chartConfig.columns.errorRate'),
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
    title: t('execution.chartConfig.columns.samplingCount'),
    dataIndex: 'n',
    width: '15%'
  },
  {
    title: t('execution.chartConfig.columns.transactionCount'),
    dataIndex: 'transactions',
    width: '15%'
  },
  {
    title: t('execution.chartConfig.columns.errorCount'),
    dataIndex: 'errors',
    width: '15%'
  },
  {
    title: t('execution.chartConfig.columns.errorRate'),
    dataIndex: 'errorRate',
    width: '15%'
  }
];
