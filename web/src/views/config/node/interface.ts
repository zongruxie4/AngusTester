import { i18n } from '@xcan-angus/infra';
const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

export const sortOpt = [
  {
    name: t('node.nodeItem.interface.sortOptions.byAddTime'),
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: t('node.nodeItem.interface.sortOptions.byName'),
    key: 'name',
    orderSort: 'ASC'
  }
];

export const installConfigColumns = [
  [
    {
      dataIndex: 'tenantId',
      label: t('node.nodeItem.interface.installConfigColumns.tenantId')
    },
    {
      dataIndex: 'deviceId',
      label: t('node.nodeItem.interface.installConfigColumns.deviceId')
    },
    {
      dataIndex: 'serverCtrlUrlPrefix',
      label: t('node.nodeItem.interface.installConfigColumns.serverCtrlUrlPrefix')
    },
    {
      dataIndex: 'ctrlAccessToken',
      label: t('node.nodeItem.interface.installConfigColumns.ctrlAccessToken')
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
