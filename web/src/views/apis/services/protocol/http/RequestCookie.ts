// import { API_EXTENSION_KEY } from '@/views/apis/utils';

// const { valueKey } = API_EXTENSION_KEY;

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));
