
export type MockServiceObj = {
  id: string;
  name: string;
  source: {
      value: string;
      message: string;
  },
  status: {
    value: string;
    message: string;
  },
  currentAuths:{
    value: string;
    message: string;
  }[],
  nodeId: string;
  nodeName: string;
  serviceDomainUrl: string;
  serviceHostUrl: string;
  authFlag: boolean;
  hasAuth: boolean;
  assocProjectFlag: boolean;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  failTips?:string[];
  currentAuthsValue:string[]
}

export interface TableSelection {
  selectedRowKeys?: string[];
  onChange?: (selectedRowKeys: string[]) => void;
  getCheckboxProps?: (record: MockServiceObj) => object;
}
