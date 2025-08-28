import type { Dayjs } from 'dayjs';

export type ShareInfo = {
  id: string;
  name: string;
  remark: string;
  expiredDate: Dayjs;
  displayOptions: DisplayOptions;
  shareScope: 'SERVICES' | 'PARTIAL_APIS' | 'SINGLE_APIS';
  servicesId: string;
  apisIds: string[];
  url: string;
  viewNum: number;
  isExpired: boolean;
  createdByName: string;
  createdDate: Dayjs;
  lastModifiedByName: string;
  lastModifiedDate: Dayjs;
}

export type DisplayOptions = {
  includeServiceInfo: boolean;
  allowDebug: boolean;
  schemaStyle: 'TREE' | 'TABLE';
}

export type IPane = {
  _id: string;
  name: string;
  value: string;
  closable?: boolean;
  forceRender?: boolean;
  icon?: string;
  active?: boolean;

  data?: { [key: string]: any; };
};
