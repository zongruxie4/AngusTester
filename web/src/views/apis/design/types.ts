import type { Dayjs } from 'dayjs';
import { EnumMessage } from '@xcan-angus/infra';
import { ApisDesignSource } from '@/enums/enums';

export const DEFAULT_OPENAPI_VERSION = ['3.0.0', '3.0.1', '3.0.2', '3.0.3', '3.1.0'];
export const SUPPORTED_OPENAPI_VERSION = ['3.0.0', '3.0.1', '3.0.2', '3.0.3', '3.1.0'];
export const SUPPORTED_OPENAPI_VERSION_OPTION = SUPPORTED_OPENAPI_VERSION.map(i => ({ value: i, label: i }));

export type DisplayOptions = {
  includeServiceInfo: boolean;
  allowDebug: boolean;
  schemaStyle: 'TREE' | 'TABLE';
}

export type ApiDesignInfo = {
  id: number;
  projectId: number;
  name: string;
  released: boolean;
  openapiSpecVersion: string;
  designSource: EnumMessage<ApisDesignSource>;
  designSourceId: number;
  designSourceName: string;
  tenantId: number;
  createdBy: number;
  createdByName: string;
  createdByAvatar: string;
  createdDate: Dayjs;
  lastModifiedBy: number;
  lastModifiedByName: string;
  lastModifiedDate: Dayjs;
}
