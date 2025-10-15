
export type AuthObj = {
  in: {
      value: 'header' | 'query';
      message: 'header' | 'query';
  },
  keyName: string;
  value: string;
  keyNameErr: boolean;
  valueErr: boolean;
}

export type SyncObj = {
  id: string;
  syncSource: 'OpenAPI';
  projectId: string;
  name: string;
  apiDocsUrl: string;
  strategyWhenDuplicated: {
      value: 'COVER' | 'IGNORE',
      message: string;
  },
  deleteWhenNotExisted: false,
  auths: AuthObj[],
  syncSuccessFlag?: boolean;
  syncFailureCause?: string;
  lastSyncDate?: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  auth:boolean;
  isEdit:boolean;
  isExpand:boolean;
  isAdd:boolean;
  testLoading: boolean;
  delLoading: boolean;
  syncLoading: boolean;
  saveloading: boolean;
  nameErr:boolean;
  apiDocsUrlErr:{
    emptyUrl:boolean,
    errUrl:boolean
  };
}
