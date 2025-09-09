import { ParamsItem } from '../../interface';

export interface State {
  formData: ParamsItem[]
}

export const itemTypes = [
  'string',
  'boolean',
  'integer',
  'number'
].map(i => ({ value: i, label: i }));

export const formDataTypes = [
  'string',
  'boolean',
  'integer',
  'number',
  'file',
  'file(array)'
].map(i => ({ value: i, label: i }));
