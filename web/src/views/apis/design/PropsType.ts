import type { Dayjs } from 'dayjs';

export type DisplayOptions = {
  includeServiceInfo: boolean;
  allowDebug: boolean;
  schemaStyle: 'TREE' | 'TABLE';
}

export type DesignInfo = {
  id: string;
  name: string;
  remark: string;
  expiredDate: Dayjs;
  displayOptions: DisplayOptions;
  designScope: 'SERVICES' | 'PARTIAL_APIS' | 'SINGLE_APIS';
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
