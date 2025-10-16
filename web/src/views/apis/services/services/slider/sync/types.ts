import { EnumMessage } from '@xcan-angus/infra';
import { StrategyWhenDuplicated } from '@/enums/enums';

export type SyncAuthInfo = {
  keyName: string;
  value: string;
  keyNameErr: boolean;
  valueErr: boolean;
  in: {
    value: 'header' | 'query';
    message: 'header' | 'query';
  },
}

export type SyncConfigInfo = {
  id: string;
  name: string;
  syncSource: 'OpenAPI';
  projectId: string;
  apiDocsUrl: string;
  auth:boolean;
  strategyWhenDuplicated: EnumMessage<StrategyWhenDuplicated>,
  deleteWhenNotExisted: false,
  auths: SyncAuthInfo[],
  syncSuccess?: boolean;
  syncFailureCause?: string;
  lastSyncDate?: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;

  // Temp fileds in web
  isEdit:boolean;
  isExpand:boolean;
  isAdd:boolean;
  testLoading: boolean;
  delLoading: boolean;
  syncLoading: boolean;
  saveLoading: boolean;
  nameErr:boolean;
  apiDocsUrlErr:{
    emptyUrl:boolean,
    errUrl:boolean
  };
}
