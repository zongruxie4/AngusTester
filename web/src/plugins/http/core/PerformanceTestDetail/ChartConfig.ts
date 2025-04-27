export const allCvsKeys = ['duration', 'errors', 'iterations', 'n', 'operations', 'transactions', 'readBytes', 'writeBytes', 'ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps', 'brps', 'minBrps', 'maxBrps', 'meanBrps', 'bwps', 'minBwps', 'maxBwps', 'meanBwps', 'tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999', 'errorRate', 'threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];
export const allCvsNames = {
  duration: '时长',
  errors: '错误数',
  iterations: '迭代数',
  n: '采样数',
  operations: '请求数',
  transactions: '事务数',
  readBytes: '读数据大小',
  writeBytes: '写数据大小',
  ops: '每秒查询数',
  tps: '每秒事务数',
  brps: '下载/秒',
  bwps: '上传/秒',
  tranMean: '平均',
  tranMin: '最小',
  tranMax: '最大',
  tranP50: 'P50',
  tranP75: 'P75',
  tranP90: 'P90',
  tranP95: 'P95',
  tranP99: 'P99',
  tranP999: 'P999',
  errorRate: '错误率',
  threadPoolSize: '线程数',
  threadPoolActiveSize: '活跃线程数',
  threadMaxPoolSize: '最大线程数'
};

export const allColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    width: '8%',
    ellipsis: true
  },
  {
    title: '采样数',
    dataIndex: 'n',
    width: '6%'
  },
  {
    title: '请求数',
    dataIndex: 'operations',
    width: '6%'
  },
  {
    title: '事务数',
    dataIndex: 'transactions',
    width: '6%'
  },
  {
    title: '平均',
    dataIndex: 'tranMean',
    width: '5%'
  },
  {
    title: '最小',
    dataIndex: 'tranMin',
    width: '5%'
  },
  {
    title: '最大',
    dataIndex: 'tranMax',
    width: '5%'
  },
  {
    title: 'P50',
    dataIndex: 'tranP50',
    width: '5%'
  },
  {
    title: 'P75',
    dataIndex: 'tranP75',
    width: '5%'
  },
  {
    title: 'P90',
    dataIndex: 'tranP90',
    width: '5%'
  },
  {
    title: 'P99',
    dataIndex: 'tranP99',
    width: '5%'
  },
  {
    title: 'P999',
    dataIndex: 'tranP999',
    width: '5%'
  },
  {
    title: '每秒查询数',
    dataIndex: 'ops',
    width: '5%'
  },
  {
    title: '每秒事务数',
    dataIndex: 'tps',
    width: '5%'
  },
  {
    title: '错误数',
    dataIndex: 'errors',
    width: '5%'
  },
  {
    title: '错误率',
    dataIndex: 'errorRate',
    width: '5%'
  },
  {
    title: '下载/秒',
    dataIndex: 'brps',
    width: '7%'
  },
  {
    title: '上传/秒',
    dataIndex: 'bwps',
    width: '7%'
  }
];

export const allResponseTimeColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '平均',
    dataIndex: 'tranMean',
    width: '9.5%'
  },
  {
    title: '最小',
    dataIndex: 'tranMin',
    width: '9.5%'
  },
  {
    title: '最大',
    dataIndex: 'tranMax',
    width: '9.5%'
  },
  {
    title: 'P50',
    dataIndex: 'tranP50',
    width: '9.5%'
  },
  {
    title: 'P75',
    dataIndex: 'tranP75',
    width: '9.5%'
  },
  {
    title: 'P90',
    dataIndex: 'tranP90',
    width: '9.5%'
  },
  {
    title: 'P95',
    dataIndex: 'tranP95',
    width: '9.5%'
  },
  {
    title: 'P99',
    dataIndex: 'tranP99',
    width: '9.5%'
  },
  {
    title: 'P999',
    dataIndex: 'tranP999',
    width: '9.5%'
  }
];

export const allErrorsColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '错误数',
    dataIndex: 'errors'
  }
];

export const allErrorRateColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '错误率',
    dataIndex: 'errorRate'
  }
];

export const allUploadColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '下载/秒',
    dataIndex: 'brps',
    width: '40%'
  },
  {
    title: '上传/秒',
    dataIndex: 'bwps',
    width: '40%'
  }
];

export const oneUploadColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '下载/秒(当前 | 最小 | 最大 | 平均)',
    dataIndex: 'brps',
    width: '40%'
  },
  {
    title: '上传/秒(当前 | 最小 | 最大 | 平均)',
    dataIndex: 'bwps',
    width: '40%'
  }
];

export const allRowsColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '迭代数',
    dataIndex: 'iterations',
    width: '25%'
  },
  {
    title: '事务数',
    dataIndex: 'transactions',
    width: '25%'
  },
  {
    title: '错误数',
    dataIndex: 'errors',
    width: '25%'
  }
];

export const throughputCvsKeys = ['ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps'];

export const throughputOptions = [
  {
    label: '每秒查询数',
    value: 'ops'
  },
  {
    label: '每秒事务数',
    value: 'tps'
  }
];

export const oneThroughputColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '当前',
    dataIndex: 'tps',
    width: '20%'
  },
  {
    title: '最小',
    dataIndex: 'minTps',
    width: '20%'
  },
  {
    title: '最大',
    dataIndex: 'maxTps',
    width: '20%'
  },
  {
    title: '平均',
    dataIndex: 'meanTps',
    width: '20%'
  }
];

export const throughputColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '每秒查询数(当前 | 最小 | 最大 | 平均)',
    dataIndex: 'multip-ops',
    width: '40%'
  },
  {
    title: '每秒事务数(当前 | 最小 | 最大 | 平均)',
    dataIndex: 'multip-tps',
    width: '40%'
  }
];

export const threadCvsKeys = ['threadPoolSize', 'threadPoolActiveSize', 'threadMaxPoolSize'];

export const threadOptions = [
  {
    label: '线程数',
    value: 'threadPoolSize'
  },
  {
    label: '活跃线程数',
    value: 'threadPoolActiveSize'
  },
  {
    label: '最大线程数',
    value: 'threadMaxPoolSize'
  }
];

export const threadColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '线程数',
    dataIndex: 'threadPoolSize',
    width: '22%'
  },
  {
    title: '活跃线程数',
    dataIndex: 'threadPoolActiveSize',
    width: '22%'
  },
  {
    title: '最大线程数',
    dataIndex: 'threadMaxPoolSize',
    width: '22%'
  }
];

export const responseTimeCvsKeys = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];

export const responseTimeOptions = [
  {
    label: '平均',
    value: 'tranMean'
  },
  {
    label: '最小',
    value: 'tranMin'
  },
  {
    label: '最大',
    value: 'tranMax'
  },
  {
    label: 'P50',
    value: 'tranP50'
  },
  {
    label: 'P75',
    value: 'tranP75'
  },
  {
    label: 'P90',
    value: 'tranP90'
  },
  {
    label: 'P95',
    value: 'tranP95'
  },
  {
    label: 'P99',
    value: 'tranP99'
  },
  {
    label: 'P999',
    value: 'tranP999'
  }
];

export const responseTimeColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '平均',
    dataIndex: 'tranMean',
    width: '8%'
  },
  {
    title: '最小',
    dataIndex: 'tranMin',
    width: '8%'
  },
  {
    title: '最大',
    dataIndex: 'tranMax',
    width: '8%'
  },
  {
    title: 'P50',
    dataIndex: 'tranP50',
    width: '8%'
  },
  {
    title: 'P75',
    dataIndex: 'tranP75',
    width: '8%'
  },
  {
    title: 'P90',
    dataIndex: 'tranP90',
    width: '8%'
  },
  {
    title: 'P95',
    dataIndex: 'tranP95',
    width: '8%'
  },
  {
    title: 'P99',
    dataIndex: 'tranP99',
    width: '8%'
  },
  {
    title: 'P999',
    dataIndex: 'tranP999',
    width: '8%'
  }
];

export const errorCvsKeys = ['errors', 'n', 'operations', 'transactions', 'errorRate'];

export const errorOptions = [
  {
    label: '采样数',
    value: 'n'
  },
  {
    label: '请求数',
    value: 'operations'
  },
  {
    label: '事务数',
    value: 'transactions'
  },
  {
    label: '错误数',
    value: 'errors'
  },
  {
    label: '错误率',
    value: 'errorRate'
  }
];
export const errorColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '采样数',
    dataIndex: 'n',
    width: '12%'
  },
  {
    title: '请求数',
    dataIndex: 'operations',
    width: '12%'
  },
  {
    title: '事务数',
    dataIndex: 'transactions',
    width: '12%'
  },
  {
    title: '错误数',
    dataIndex: 'errors',
    width: '12%'
  },
  {
    title: '错误率',
    dataIndex: 'errorRate',
    width: '12%'
  }
];

export const oneApiErrorColumns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '采样数',
    dataIndex: 'n',
    width: '15%'
  },
  {
    title: '事务数',
    dataIndex: 'transactions',
    width: '15%'
  },
  {
    title: '错误数',
    dataIndex: 'errors',
    width: '15%'
  },
  {
    title: '错误率',
    dataIndex: 'errorRate',
    width: '15%'
  }
];
