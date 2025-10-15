import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps', 'brps', 'minBrps', 'maxBrps', 'meanBrps', 'bwps', 'minBwps', 'maxBwps', 'meanBwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const allCvsNames = {
  duration: t('common.duration'),
  errors: t('commonPlugin.chartConfig.names.errors'),
  iterations: t('commonPlugin.chartConfig.names.iterations'),
  n: t('commonPlugin.chartConfig.names.n'),
  operations: t('commonPlugin.chartConfig.names.operations'),
  transactions: t('commonPlugin.chartConfig.names.transactions'),
  readBytes: t('commonPlugin.chartConfig.names.readBytes'),
  writeBytes: t('commonPlugin.chartConfig.names.writeBytes'),
  ops: t('commonPlugin.chartConfig.names.ops'),
  tps: t('commonPlugin.chartConfig.names.tps'),
  brps: t('commonPlugin.chartConfig.names.brps'),
  bwps: t('commonPlugin.chartConfig.names.bwps'),
  tranMean: t('commonPlugin.chartConfig.names.tranMean'),
  tranMin: t('commonPlugin.chartConfig.names.tranMin'),
  tranMax: t('commonPlugin.chartConfig.names.tranMax'),
  tranP50: t('commonPlugin.chartConfig.names.tranP50'),
  tranP75: t('commonPlugin.chartConfig.names.tranP75'),
  tranP90: t('commonPlugin.chartConfig.names.tranP90'),
  tranP95: t('commonPlugin.chartConfig.names.tranP95'),
  tranP99: t('commonPlugin.chartConfig.names.tranP99'),
  tranP999: t('commonPlugin.chartConfig.names.tranP999'),
  errorRate: t('commonPlugin.chartConfig.names.errorRate'),
  threadPoolSize: t('commonPlugin.chartConfig.names.threadPoolSize'),
  threadPoolActiveSize: t('commonPlugin.chartConfig.names.threadPoolActiveSize'),
  threadMaxPoolSize: t('commonPlugin.chartConfig.names.threadMaxPoolSize')
};

export const allColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    width: '8%',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.names.n'),
    dataIndex: 'n',
    width: '6%'
  },
  {
    title: t('commonPlugin.chartConfig.names.operations'),
    dataIndex: 'operations',
    width: '6%'
  },
  {
    title: t('commonPlugin.chartConfig.names.transactions'),
    dataIndex: 'transactions',
    width: '6%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMean'),
    dataIndex: 'tranMean',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMin'),
    dataIndex: 'tranMin',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMax'),
    dataIndex: 'tranMax',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP50'),
    dataIndex: 'tranP50',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP75'),
    dataIndex: 'tranP75',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP90'),
    dataIndex: 'tranP90',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP99'),
    dataIndex: 'tranP99',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP999'),
    dataIndex: 'tranP999',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.ops'),
    dataIndex: 'ops',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tps'),
    dataIndex: 'tps',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errors'),
    dataIndex: 'errors',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errorRate'),
    dataIndex: 'errorRate',
    width: '5%'
  },
  {
    title: t('commonPlugin.chartConfig.columns.downloadPerSecond'),
    dataIndex: 'brps',
    width: '7%'
  },
  {
    title: t('commonPlugin.chartConfig.columns.uploadPerSecond'),
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
    title: t('commonPlugin.chartConfig.names.tranMean'),
    dataIndex: 'tranMean',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMin'),
    dataIndex: 'tranMin',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMax'),
    dataIndex: 'tranMax',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP50'),
    dataIndex: 'tranP50',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP75'),
    dataIndex: 'tranP75',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP90'),
    dataIndex: 'tranP90',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP95'),
    dataIndex: 'tranP95',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP99'),
    dataIndex: 'tranP99',
    width: '9.5%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP999'),
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
    title: t('commonPlugin.chartConfig.names.errors'),
    dataIndex: 'errors'
  }
];

export const allErrorRateColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.names.errorRate'),
    dataIndex: 'errorRate'
  }
];

export const allUploadColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.columns.downloadPerSecond'),
    dataIndex: 'brps',
    width: '40%'
  },
  {
    title: t('commonPlugin.chartConfig.columns.uploadPerSecond'),
    dataIndex: 'bwps',
    width: '40%'
  }
];

export const oneUploadColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.columns.downloadPerSecondWithStats'),
    dataIndex: 'brps',
    width: '40%'
  },
  {
    title: t('commonPlugin.chartConfig.columns.uploadPerSecondWithStats'),
    dataIndex: 'bwps',
    width: '40%'
  }
];

export const allRowsColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.names.iterations'),
    dataIndex: 'iterations',
    width: '25%'
  },
  {
    title: t('commonPlugin.chartConfig.names.transactions'),
    dataIndex: 'transactions',
    width: '25%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errors'),
    dataIndex: 'errors',
    width: '25%'
  }
];

export const throughputCvsKeys = ['ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps'];

export const throughputOptions = [
  {
    label: t('commonPlugin.chartConfig.names.ops'),
    value: 'ops'
  },
  {
    label: t('commonPlugin.chartConfig.names.tps'),
    value: 'tps'
  }
];

export const oneThroughputColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.columns.current'),
    dataIndex: 'tps',
    width: '20%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMin'),
    dataIndex: 'minTps',
    width: '20%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMax'),
    dataIndex: 'maxTps',
    width: '20%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMean'),
    dataIndex: 'meanTps',
    width: '20%'
  }
];

export const throughputColumns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: t('commonPlugin.chartConfig.columns.opsWithStats'),
    dataIndex: 'ops',
    width: '40%'
  },
  {
    title: t('commonPlugin.chartConfig.columns.tpsWithStats'),
    dataIndex: 'tps',
    width: '40%'
  }
];

export const threadCvsKeys = ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const threadOptions = [
  {
    label: t('commonPlugin.chartConfig.names.threadPoolSize'),
    value: 'threadPoolSize'
  },
  {
    label: t('commonPlugin.chartConfig.names.threadPoolActiveSize'),
    value: 'threadPoolActiveSize'
  },
  {
    label: t('commonPlugin.chartConfig.names.threadMaxPoolSize'),
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
    title: t('commonPlugin.chartConfig.names.threadPoolSize'),
    dataIndex: 'threadPoolSize',
    width: '22%'
  },
  {
    title: t('commonPlugin.chartConfig.names.threadPoolActiveSize'),
    dataIndex: 'threadPoolActiveSize',
    width: '22%'
  },
  {
    title: t('commonPlugin.chartConfig.names.threadMaxPoolSize'),
    dataIndex: 'threadMaxPoolSize',
    width: '22%'
  }
];

export const responseTimeCvsKeys = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];

export const responseTimeOptions = [
  {
    label: t('commonPlugin.chartConfig.names.tranMean'),
    value: 'tranMean'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranMin'),
    value: 'tranMin'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranMax'),
    value: 'tranMax'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP50'),
    value: 'tranP50'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP75'),
    value: 'tranP75'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP90'),
    value: 'tranP90'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP95'),
    value: 'tranP95'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP99'),
    value: 'tranP99'
  },
  {
    label: t('commonPlugin.chartConfig.names.tranP999'),
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
    title: t('commonPlugin.chartConfig.names.tranMean'),
    dataIndex: 'tranMean',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMin'),
    dataIndex: 'tranMin',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranMax'),
    dataIndex: 'tranMax',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP50'),
    dataIndex: 'tranP50',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP75'),
    dataIndex: 'tranP75',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP90'),
    dataIndex: 'tranP90',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP95'),
    dataIndex: 'tranP95',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP99'),
    dataIndex: 'tranP99',
    width: '8%'
  },
  {
    title: t('commonPlugin.chartConfig.names.tranP999'),
    dataIndex: 'tranP999',
    width: '8%'
  }
];

export const errorCvsKeys = ['errors', 'n', 'operations', 'transactions', 'errorRate'];

export const errorOptions = [
  {
    label: t('commonPlugin.chartConfig.names.n'),
    value: 'n'
  },
  {
    label: t('commonPlugin.chartConfig.names.operations'),
    value: 'operations'
  },
  {
    label: t('commonPlugin.chartConfig.names.transactions'),
    value: 'transactions'
  },
  {
    label: t('commonPlugin.chartConfig.names.errors'),
    value: 'errors'
  },
  {
    label: t('commonPlugin.chartConfig.names.errorRate'),
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
    title: t('commonPlugin.chartConfig.names.n'),
    dataIndex: 'n',
    width: '12%'
  },
  {
    title: t('commonPlugin.chartConfig.names.operations'),
    dataIndex: 'operations',
    width: '12%'
  },
  {
    title: t('commonPlugin.chartConfig.names.transactions'),
    dataIndex: 'transactions',
    width: '12%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errors'),
    dataIndex: 'errors',
    width: '12%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errorRate'),
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
    title: t('commonPlugin.chartConfig.names.n'),
    dataIndex: 'n',
    width: '15%'
  },
  {
    title: t('commonPlugin.chartConfig.names.transactions'),
    dataIndex: 'transactions',
    width: '15%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errors'),
    dataIndex: 'errors',
    width: '15%'
  },
  {
    title: t('commonPlugin.chartConfig.names.errorRate'),
    dataIndex: 'errorRate',
    width: '15%'
  }
];
