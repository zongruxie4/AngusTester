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
