import { ParamsItem } from '../interface';
import { getModelDataByRef } from '@/views/apis/utils';
import { deconstruct } from '@/utils/swagger';
// import qs from 'qs';
// import { dataURLtoBlob, isBase64 } from '@/utils/blob';

export const radioGroups = [null, 'application/x-www-form-urlencoded', 'multipart/form-data', 'application/octet-stream'];
export const rawTypeOptions = [
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

export const enum RadioItem {
  encoded = 'application/x-www-form-urlencoded',
  formData = 'multipart/form-data',
  raw = 'raw',
  stream = 'application/octet-stream'
}

export interface RequestBodyParam {
  [key:string]: any;
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
  // binartContentTypeOpt: OptionItem[]
}

// 转换 base64 文件
// const handleBinaryCt = (value) => {
//   if (isBase64(value)) {
//     const data = dataURLtoBlob(value);
//     return data;
//   }
// };

export const getRefData = async (ref, serviceId) => {
  const [error, resp] = await getModelDataByRef(serviceId, ref);
  if (error) {
    return '';
  }
  return deconstruct(resp.data || {});
};

export const transRefJsonToDataJson = async (schema = {}, serviceId) => {
  // const keys = Object.keys(schema);
  for (const key in schema) {
    if (key === '$ref') {
      const refData = await getRefData(schema.$ref, serviceId);
      schema = { ...schema, ...refData };
      delete schema.$ref;
    }
    if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
      schema[key] = await transRefJsonToDataJson(schema[key], serviceId);
    }
  }
  return schema;
};

export const deepParseJson = (jsonStr: string) => {
  try {
    const result = JSON.parse(jsonStr);
    if (typeof result === 'string') {
      return deepParseJson(result);
    }
    return result;
  } catch {
    return jsonStr;
  }
};
