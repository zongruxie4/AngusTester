
export const sortOpt = [
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: '按名称',
    key: 'name',
    orderSort: 'ASC'
  }
];

export const installConfigColumns = [
  [
    {
      dataIndex: 'tenantId',
      label: '租户ID'
    },
    {
      dataIndex: 'deviceId',
      label: '设备ID'
    },
    {
      dataIndex: 'ctrlAccessToken',
      label: '设备访问令牌'
    }
  ]
];

export const getDefaultNode = () => {
  return {
    editable: false,
    name: '',
    tags: [],
    id: '',
    pubilcIp: '',
    roles: [],
    nodeRoles: [],
    sshPort: '22',
    password: '',
    ip: '',
    spec: {}
  };
};

export const getStrokeColor = (percent: number) => {
  if (percent > 85) {
    return '#F5222D';
  }
  if (percent >= 65) {
    return '#FFB925';
  }
  if (percent > 0) {
    return '#52C41A';
  }
};

export interface Params {
  name?: string,
  nodeRole?: string,
  createdBy?: string,
  source?: string,
  orderBy?:string,
  orderSort?: string,
  'filters[0].key'?: string,
  'filters[0].op'?: string,
  'filters[0].value'?: string,
  'filters[1].key'?: string,
  'filters[1].op'?: string,
  'filters[1].value'?: string,
}
