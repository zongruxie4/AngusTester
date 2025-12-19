import type { Dayjs } from 'dayjs';
import { EnumMessage } from '@xcan-angus/infra';
import { ApisShareScope } from '@/enums/enums';

export type DisplayOptions = {
  includeServiceInfo: boolean;
  allowDebug: boolean;
  schemaStyle: 'TREE' | 'TABLE';
}

export type ShareInfo = {
  id: string;
  name: string;
  remark: string;
  expiredDate: Dayjs;
  displayOptions: DisplayOptions;
  shareScope: EnumMessage<ApisShareScope>;
  servicesId: string;
  apisIds: string[];
  url: string;
  viewNum: number;
  isExpired: boolean;
  creator: string;
  createdDate: Dayjs;
  modifier: string;
  modifiedDate: Dayjs;
}

export type ShareEditForm = {
  name?: string;
  remark?: string;
  expiredDate?: string;
  displayOptions: DisplayOptions;
  shareScope: ApisShareScope;
  servicesId?: string;
  apisIds: string[];
}
