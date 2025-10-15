import { i18n } from '@xcan-angus/infra';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string): string => value);

export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'tps', 'brps', 'bwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];
export const allCvsNames = {
  duration: t('common.duration'),
  errors: t('xcan_exec_chartConfig.errors'),
  iterations: t('xcan_exec_chartConfig.iterations'),
  n: t('xcan_exec_chartConfig.sampleCount'),
  operations: t('xcan_exec_chartConfig.requestCount'),
  transactions: t('xcan_exec_chartConfig.transactionCount'),
  readBytes: t('xcan_exec_chartConfig.readDataSize'),
  writeBytes: t('xcan_exec_chartConfig.writeDataSize'),
  ops: t('xcan_exec_chartConfig.queriesPerSecond'),
  tps: t('xcan_exec_chartConfig.transactionsPerSecond'),
  brps: t('xcan_exec_chartConfig.downloadPerSecond'),
  bwps: t('xcan_exec_chartConfig.uploadPerSecond'),
  tranMean: t('chart.average'),
  tranMin: t('chart.min'),
  tranMax: t('chart.max'),
  tranP50: t('chart.p50'),
  tranP75: t('chart.p75'),
  tranP90: t('chart.p90'),
  tranP95: t('chart.p95'),
  tranP99: t('chart.p99'),
  tranP999: t('xcan_exec_chartConfig.p999'),
  errorRate: t('xcan_exec_chartConfig.errorRate'),
  threadPoolSize: t('xcan_exec_chartConfig.threadCount'),
  threadPoolActiveSize: t('xcan_exec_chartConfig.activeThreadCount'),
  threadMaxPoolSize: t('xcan_exec_chartConfig.maxThreadCount')
};

export const allColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    width: '11.5%',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.sampleCount'),
    dataIndex: 'n',
    width: '7%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionCount'),
    dataIndex: 'transactions',
    width: '7%'
  },
  {
    title: t('chart.average'),
    dataIndex: 'tranMean',
    width: '5%'
  },
  {
    title: t('chart.min'),
    dataIndex: 'tranMin',
    width: '5%'
  },
  {
    title: t('chart.max'),
    dataIndex: 'tranMax',
    width: '5%'
  },
  {
    title: t('chart.p50'),
    dataIndex: 'tranP50',
    width: '5%'
  },
  {
    title: t('chart.p75'),
    dataIndex: 'tranP75',
    width: '5%'
  },
  {
    title: t('chart.p90'),
    dataIndex: 'tranP90',
    width: '5%'
  },
  {
    title: t('chart.p99'),
    dataIndex: 'tranP99',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.p999'),
    dataIndex: 'tranP999',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.queriesPerSecond'),
    dataIndex: 'ops',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionsPerSecond'),
    dataIndex: 'tps',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.errors'),
    dataIndex: 'errors',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '5%'
  },
  {
    title: t('xcan_exec_chartConfig.downloadPerSecond'),
    dataIndex: 'brps',
    width: '7%'
  },
  {
    title: t('xcan_exec_chartConfig.uploadPerSecond'),
    dataIndex: 'bwps',
    width: '7%'
  }
];

export const allResponseTimeColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('chart.average'),
    dataIndex: 'tranMean',
    width: '9.5%'
  },
  {
    title: t('chart.min'),
    dataIndex: 'tranMin',
    width: '9.5%'
  },
  {
    title: t('chart.max'),
    dataIndex: 'tranMax',
    width: '9.5%'
  },
  {
    title: t('chart.p50'),
    dataIndex: 'tranP50',
    width: '9.5%'
  },
  {
    title: t('chart.p75'),
    dataIndex: 'tranP75',
    width: '9.5%'
  },
  {
    title: t('chart.p90'),
    dataIndex: 'tranP90',
    width: '9.5%'
  },
  {
    title: t('chart.p95'),
    dataIndex: 'tranP95',
    width: '9.5%'
  },
  {
    title: t('chart.p99'),
    dataIndex: 'tranP99',
    width: '9.5%'
  },
  {
    title: t('xcan_exec_chartConfig.p999'),
    dataIndex: 'tranP999',
    width: '9.5%'
  }
];

export const allErrorsColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.errors'),
    dataIndex: 'errors',
    width: '66%'
  }
];

export const allErrorRateColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '66%'
  }
];

export const allUploadColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.downloadPerSecond'),
    dataIndex: 'brps',
    width: '33%'
  },
  {
    title: t('xcan_exec_chartConfig.uploadPerSecond'),
    dataIndex: 'bwps',
    width: '33%'
  }
];

export const allRowsColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.iterations'),
    dataIndex: 'iterations',
    width: '25%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionCount'),
    dataIndex: 'transactions',
    width: '25%'
  },
  {
    title: t('xcan_exec_chartConfig.errors'),
    dataIndex: 'errors',
    width: '25%'
  }
];

export const throughputCvsKeys = ['ops', 'tps', 'brps', 'bwps'];

export const throughputOptions = [
  {
    label: t('xcan_exec_chartConfig.queriesPerSecond'),
    value: 'ops'
  },
  {
    label: t('xcan_exec_chartConfig.transactionsPerSecond'),
    value: 'tps'
  },
  {
    label: t('xcan_exec_chartConfig.downloadPerSecond'),
    value: 'brps'
  },
  {
    label: t('xcan_exec_chartConfig.uploadPerSecond'),
    value: 'bwps'
  }
];

export const throughputColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.queriesPerSecond'),
    dataIndex: 'ops',
    width: '16.5%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionsPerSecond'),
    dataIndex: 'tps',
    width: '16.5%'
  },
  {
    title: t('xcan_exec_chartConfig.downloadPerSecond'),
    dataIndex: 'brps',
    width: '16.5%'
  },
  {
    title: t('xcan_exec_chartConfig.uploadPerSecond'),
    dataIndex: 'bwps',
    width: '16.5%'
  }
];

export const threadCvsKeys = ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const threadOptions = [
  {
    label: t('xcan_exec_chartConfig.threadCount'),
    value: 'threadPoolSize'
  },
  {
    label: t('xcan_exec_chartConfig.activeThreadCount'),
    value: 'threadPoolActiveSize'
  },
  {
    label: t('xcan_exec_chartConfig.maxThreadCount'),
    value: 'threadMaxPoolSize'
  }
];

export const threadColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.threadCount'),
    dataIndex: 'threadPoolSize',
    width: '22%'
  },
  {
    title: t('xcan_exec_chartConfig.activeThreadCount'),
    dataIndex: 'threadPoolActiveSize',
    width: '22%'
  },
  {
    title: t('xcan_exec_chartConfig.maxThreadCount'),
    dataIndex: 'threadMaxPoolSize',
    width: '22%'
  }
];

export const responseTimeCvsKeys = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];

export const responseTimeOptions = [
  {
    label: t('chart.average'),
    value: 'tranMean'
  },
  {
    label: t('chart.min'),
    value: 'tranMin'
  },
  {
    label: t('chart.max'),
    value: 'tranMax'
  },
  {
    label: t('chart.p50'),
    value: 'tranP50'
  },
  {
    label: t('chart.p75'),
    value: 'tranP75'
  },
  {
    label: t('chart.p90'),
    value: 'tranP90'
  },
  {
    label: t('chart.p95'),
    value: 'tranP95'
  },
  {
    label: t('chart.p99'),
    value: 'tranP99'
  },
  {
    label: t('xcan_exec_chartConfig.p999'),
    value: 'tranP999'
  }
];

export const responseTimeColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('chart.average'),
    dataIndex: 'tranMean',
    width: '8%'
  },
  {
    title: t('chart.min'),
    dataIndex: 'tranMin',
    width: '8%'
  },
  {
    title: t('chart.max'),
    dataIndex: 'tranMax',
    width: '8%'
  },
  {
    title: t('chart.p50'),
    dataIndex: 'tranP50',
    width: '8%'
  },
  {
    title: t('chart.p75'),
    dataIndex: 'tranP75',
    width: '8%'
  },
  {
    title: t('chart.p90'),
    dataIndex: 'tranP90',
    width: '8%'
  },
  {
    title: t('chart.p95'),
    dataIndex: 'tranP95',
    width: '8%'
  },
  {
    title: t('chart.p99'),
    dataIndex: 'tranP99',
    width: '8%'
  },
  {
    title: t('xcan_exec_chartConfig.p999'),
    dataIndex: 'tranP999',
    width: '8%'
  }
];

export const errorCvsKeys = ['errors', 'n', 'operations', 'transactions', 'errorRate'];

export const errorOptions = [
  {
    label: t('xcan_exec_chartConfig.sampleCount'),
    value: 'n'
  },
  {
    label: t('xcan_exec_chartConfig.requestCount'),
    value: 'operations'
  },
  {
    label: t('xcan_exec_chartConfig.transactionCount'),
    value: 'transactions'
  },
  {
    label: t('xcan_exec_chartConfig.errors'),
    value: 'errors'
  },
  {
    label: t('xcan_exec_chartConfig.errorRate'),
    value: 'errorRate'
  }
];
export const errorColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.sampleCount'),
    dataIndex: 'n',
    width: '12%'
  },
  {
    title: t('xcan_exec_chartConfig.requestCount'),
    dataIndex: 'operations',
    width: '12%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionCount'),
    dataIndex: 'transactions',
    width: '12%'
  },
  {
    title: t('xcan_exec_chartConfig.errors'),
    dataIndex: 'errors',
    width: '12%'
  },
  {
    title: t('xcan_exec_chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '12%'
  }
];

export const oneApiErrorColumns = [
  {
    title: t('xcan_exec_chartConfig.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('xcan_exec_chartConfig.sampleCount'),
    dataIndex: 'n',
    width: '15%'
  },
  {
    title: t('xcan_exec_chartConfig.transactionCount'),
    dataIndex: 'transactions',
    width: '15%'
  },
  {
    title: t('xcan_exec_chartConfig.errors'),
    dataIndex: 'errors',
    width: '15%'
  },
  {
    title: t('xcan_exec_chartConfig.errorRate'),
    dataIndex: 'errorRate',
    width: '15%'
  }
];
