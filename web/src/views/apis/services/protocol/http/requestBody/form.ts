import { ParamsItem } from '../interface';

export interface State {
  formData: ParamsItem[]
}

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));

export const formDataTypes = [
  'string',
  'array(json)',
  'array(xml)',
  'boolean',
  'integer',
  'object(json)',
  'object(xml)',
  'number',
  'file',
  'file(array)'
].map(i => ({ value: i, label: i }));
