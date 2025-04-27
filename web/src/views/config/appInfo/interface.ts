
export interface Params {
  filters: { key: string, op: string, value: string }[]
}

export interface TableData {
  policyNames: string[],
  avatar?: string,
  createdDate: string,
  name?:string,
  fullname?: string,
  id:string
}

export interface Policy {
  id: string;
  name: string;
  code: string;
  orgType: {
    value: string;
    message: string;
  };
  createdDate: string;
}

export interface AppInfo {
  authCtrlFlag: boolean,
  clientId: string | undefined,
  clientName: string,
  code: string,
  createdBy: string,
  createdDate: string,
  description: string,
  enabled: undefined,
  createdByName: string,
  icon: string,
  id: string,
  name: string,
  openStage: { value: string, message: string },
  sequence: number,
  showName: string,
  tags: any[],
  tenantId: string,
  tenantName: string,
  type: { value: string, message: string },
  url: string,
  version: { major: number, minor: number, patch: number },
  exportLoading: boolean
}
