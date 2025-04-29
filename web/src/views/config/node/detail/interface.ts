
export const infoItem = [
  [{
    label: '名称',
    dataIndex: 'name'
  },
  {
    label: '用户名',
    dataIndex: 'username'
  },
  {
    label: '来源',
    dataIndex: 'sourceName'
  },
  {
    label: '域名',
    dataIndex: 'domain'
  }],
  [{
    label: '启用状态',
    dataIndex: 'enabled'
  },
  {
    label: '内网IP地址',
    dataIndex: 'ip'
  },
  {
    label: '密码',
    dataIndex: 'password'
  },
  {
    label: '角色',
    dataIndex: 'roles'
  }
  ],
  [{
    label: '代理状态',
    dataIndex: 'installAgentFlag'
  },
  {
    label: '公网IP地址',
    dataIndex: 'publicIp'
  },
  {
    label: '规格',
    dataIndex: 'spec'
  },
  {
    label: '端口',
    dataIndex: 'sshPort'
  }
  ],
  [{
    label: '连接状态',
    dataIndex: 'onlineFlag'
  },
  {
    label: '添加人',
    dataIndex: 'createdByName'
  },
  {
    label: '添加时间',
    dataIndex: 'createdDate'
  },
  {
    label: '到期时间',
    dataIndex: 'instanceExpiredDate'
  }]
];

export const nodeUseProgresses = [
  {
    label: 'CPU',
    value: 90,
    valueKey: 'cpu',
    totalKey: 'cpuTotal',
    percentValue: 'cpuPercent',
    unit: '%'
  },
  {
    label: '内存',
    value: 60,
    valueKey: 'memory',
    totalKey: 'memoryTotal',
    percentValue: 'memoryPercent',
    unit: ''
  },
  {
    label: '文件系统',
    value: 45,
    valueKey: 'disk',
    totalKey: 'diskTotal',
    percentValue: 'diskPercent',
    unit: ''
  },
  {
    label: '交换区',
    value: 45,
    valueKey: 'swap',
    totalKey: 'swapTotal',
    percentValue: 'swapPercent',
    unit: ''
  },
  {
    label: '网络',
    value: 29,
    valueKey: 'network',
    totalKey: 'network',
    percentValue: 'networkPercent'
  }
];

export const nodeEchartsTabs = [
  {
    label: 'CPU',
    value: 90,
    valueKey: 'cpu',
    totalKey: 'cpuTotal',
    percentValue: 'cpuPercent',
    unit: '%'
  },
  {
    label: '内存',
    value: 60,
    valueKey: 'memory',
    totalKey: 'memoryTotal',
    percentValue: 'memoryPercent',
    unit: ''
  },
  {
    label: '磁盘',
    value: 45,
    valueKey: 'disk',
    totalKey: 'diskTotal',
    percentValue: 'diskPercent',
    unit: ''
  },
  {
    label: '网络',
    value: 29,
    valueKey: 'network',
    totalKey: 'network',
    percentValue: 'networkPercent'
  }
];

export const internetInfo = [
  {
    label: '上传: ',
    valueKey: 'txBytesRate',
    unit: 'MB/s'
  },
  {
    label: ' 下载: ',
    valueKey: 'rxBytesRate',
    unit: 'MB/s'
  },
  {
    label: '总上传: ',
    valueKey: 'txBytes',
    unit: ''
  },
  {
    label: '总下载: ',
    valueKey: 'rxBytes',
    unit: ''
  }
];

export const timeOpt = [
  {
    value: '3-minute',
    label: '最近3分钟'
  },
  {
    value: '5-minute',
    label: '最近5分钟'
  },
  {
    value: '10-minute',
    label: '最近10分钟'
  },
  {
    value: '1-hour',
    label: '最近1小时'
  },
  {
    value: '3-hour',
    label: '最近3小时'
  }
];

export const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '平均值',
    dataIndex: 'average',
    key: 'average'
  },
  {
    title: '最高',
    dataIndex: 'high',
    key: 'high'
  },
  {
    title: '最低',
    dataIndex: 'low',
    key: 'low'
  },
  {
    title: '最新值',
    dataIndex: 'latest',
    key: 'latest'
  }
];
