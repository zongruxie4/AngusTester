
export interface Params {
  pageNo: number,
  pageSize: number,
  orderBy: string,
  orderSort: 'DESC' | 'ASC'
  id: string,
  filters: { key: string, op: string, value: string }[]
}

export interface TableData {
  policyNames: string[],
  avatar?: string,
  createdDate: string,
  name: string,
  id: string
}

interface Push {
  pkey: { value: string, message: string }
  receiveAddresses: null
  receiveIds: string[]
}

export interface PushSetting {
  ekey: string
  eventCode: string
  eventName: string
  eventType: { value: string, message: string }
  id: string
  noticeTypes: { value: string, message: string }[]
  pushSetting: Push[]
  pushType: { value: string, message: string }[]
  receiveSetting: any
}

export const _configColumns = [
  {
    title: '事件名称',
    dataIndex: 'eventName'
  },
  {
    title: '分类',
    dataIndex: 'targetType'
  },
  // {
  //   title: 'settingNotification.columns.c2',
  //   dataIndex: 'allowedChannelTypes',
  //   key: 'allowedChannelTypes'
  // },
  // {
  //   title: '事件编码',
  //   dataIndex: 'eventCode'
  // },
  // {
  //   title: '接收人',
  //   dataIndex: 'receiveSetting',
  //   key: 'receiveSetting'
  // },

  {
    title: '通知类型',
    dataIndex: 'noticeType'
    // customRender: ({record}) => {
    //   return (record?.receiveSetting?.receivers?.noticeTypes || []).map(i => i.message).join(' ')
    // }
  }
  // {
  //   title: 'settingNotification.columns.c4',
  //   key: 'operate',
  //   dataIndex: 'operate',
  //   align: 'center'
  // }
];

export interface PushRecord {
  description: string,
  ekey: string,
  errMsg: string,
  eventCode: string,
  eventContent: string,
  execNo: string,
  id: string,
  pushStatus: string,
  tenantId: string,
  tenantName: string,
  triggerTime: string
  type: string
}

export const _recordColumns = [
  {
    title: '事件ID',
    dataIndex: 'id',
    key: 'id',
    width: '12%'
  },
  {
    title: '事件名称',
    dataIndex: 'name',
    width: '12%',
    ellipsis: true
  },
  {
    title: '内容',
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: '接收人',
    dataIndex: 'fullname',
    width: '12%'
  },
  // {
  //   title: '业务编码',
  //   dataIndex: 'eventCode',
  //   key: 'eventCode'
  // },
  // {
  //   title: '事件描述',
  //   key: 'description',
  //   dataIndex: 'description'
  // },
  {
    title: '触发时间',
    key: 'createdDate',
    dataIndex: 'createdDate',
    width: '12%'
  },
  {
    title: '推送状态',
    dataIndex: 'pushStatus',
    key: 'pushStatus',
    width: '12%'
  }

];
