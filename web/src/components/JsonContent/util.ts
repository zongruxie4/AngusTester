import { utils } from '@xcan-angus/infra';
import { API_EXTENSION_KEY, getDataTypeFromFormat, getDataType } from '@/utils/apis/index';

const { valueKey, enabledKey } = API_EXTENSION_KEY;

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));

export const inOptions = ['path', 'query'].map(i => ({ value: i, label: i }));

export const baseTypes = [
  'string',
  'boolean',
  'integer',
  'number'
].map(i => ({ value: i, label: i }));

export const parameterIn = [
  'query',
  'path'
].map(i => ({ value: i, label: i }));

export const getDefaultItem = (config = {}) => {
  return {
    name: '',
    id: utils.uuid('api'),
    [valueKey]: '',
    type: 'string',
    [enabledKey]: true,
    ...config
  };
};

