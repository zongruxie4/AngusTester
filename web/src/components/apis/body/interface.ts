// Local imports
import { ParamsItem } from '../interface';

/**
 * Radio button group options for content type selection
 */
export const contentTypeRadioGroups = [null, 'application/x-www-form-urlencoded', 'multipart/form-data', 'application/octet-stream'];

// Legacy export alias for backward compatibility
export const radioGroups = contentTypeRadioGroups;

/**
 * Raw content type options for text-based content
 */
export const rawContentTypeOptions = [
  'application/json',
  'text/html',
  'application/xml',
  'application/javascript',
  'text/plain',
  '*/*'
];

// Legacy export alias for backward compatibility
export const rawTypeOptions = rawContentTypeOptions;

/**
 * Enum for radio button item types
 */
export const enum ContentTypeRadioItem {
  encoded = 'application/x-www-form-urlencoded',
  formData = 'multipart/form-data',
  raw = 'raw',
  stream = 'application/octet-stream'
}

// Legacy export alias for backward compatibility
export const enum RadioItem = ContentTypeRadioItem;

/**
 * Request body parameter interface
 */
export interface RequestBodyParameter {
  [key: string]: any;
  contentType?: string | null;
  formData?: ParamsItem[];
  rawContent?: string;
  binaryContentType?: string;
}

// Legacy export alias for backward compatibility
export type RequestBodyParam = RequestBodyParameter;

/**
 * Option item interface for dropdowns and selects
 */
export interface SelectOptionItem {
  value: any;
  label: string;
}

// Legacy export alias for backward compatibility
export type OptionItem = SelectOptionItem;

/**
 * Component state interface for request body management
 */
export interface RequestBodyState {
  encodeedList: ParamsItem[], // encodeed form params
  formDataList: ParamsItem[], // formData form params
  rawContent: string,
  radioOptions: OptionItem[],
  rawSelectOptions: OptionItem[],
}

// Legacy export alias for backward compatibility
export type StateItem = RequestBodyState;

// 转换 base64 文件
// const handleBinaryCt = (value) => {
//   if (isBase64(value)) {
//     const data = dataURLtoBlob(value);
//     return data;
//   }
// };

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
