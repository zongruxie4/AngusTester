import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { API_EXTENSION_KEY, deepDelAttrFromObj, getModelDataByRef } from '@/utils/apis';
import { ParamsItem } from '@/views/apis/services/protocol/types';
import { deconstruct } from '@/utils/swagger';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

const ajv = new Ajv();

addFormats(ajv);

export const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: 'query',
    description: '',
    // [exportVariableKey]: false,
    [valueKey]: '',
    [enabledKey]: true,
    schema: { type: 'string' },
    ...config
  };
};

export const getBodyDefaultItem = (config = {}) => {
  return {
    name: '',
    in: undefined,
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    type: 'string',
    ...config
  };
};

const getNewItem = (metaData: ParamsItem[], config?: ParamsItem): ParamsItem | undefined => {
  const emptyItem = metaData?.find((item) => !item.name);
  if (!emptyItem) {
    return getBodyDefaultItem(config);
  }
  return undefined;
};

export const validateType = (data, schema) => {
  delete schema.exampleSetFlag;
  const validate = ajv.compile(schema);
  const valid = validate(data);
  if (!valid) {
    return validate.errors;
  }
  return [];
};

const validateParameter = (data, schema) => {
  return validateType(data, schema);
};

const validateQueryParameter = (data) => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    errors.push(...validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj.schema, [])));
  });
  return !errors.length;
};

const validateBodyForm = (data) => {
  if (!data.length) {
    return true;
  }
  const errors: any[] = [];
  data.forEach(item => {
    const schemaObj = item;
    errors.push(...validateParameter(item[valueKey], deepDelAttrFromObj(schemaObj, [valueKey, 'types', 'exampleSetFlag', 'name', enabledKey, 'key'])));
  });
  return !errors.length;
};

export {
  getNewItem,
  validateQueryParameter,
  validateBodyForm
};

export const schemaTypeToOptions = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));

export const getRefData = async (ref, serviceId) => {
  const [error, resp] = await getModelDataByRef(serviceId, ref);
  if (error) {
    return '';
  }
  return deconstruct(resp.data || {});
};

export const transRefJsonToDataJson = async (schema: any = {}, serviceId) => {
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
