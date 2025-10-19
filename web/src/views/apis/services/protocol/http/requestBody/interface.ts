import { ParamsItem } from '@/views/apis/services/protocol/types';

export const radioGroups = [
  null, 'application/x-www-form-urlencoded',
  'multipart/form-data',
  'application/octet-stream'
];

export const rawTypeOptions = [
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

export interface RequestBodyParam {
  [key:string]: any; // TODO ?
  contentType?: string | null;
  formData?: ParamsItem[];
  rawContent?: string;
  binaryContentType?: string;
}

export interface OptionItem {
  value: any,
  label: string
}

export interface StateItem {
  encodeedList: ParamsItem[], // encodeed form params
  formDataList: ParamsItem[], // formData form params
  rawContent: string,
  radioOptions: OptionItem[],
  rawSelectOptions: OptionItem[],
}
