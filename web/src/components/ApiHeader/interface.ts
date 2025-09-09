import { API_EXTENSION_KEY } from '@xcan-angus/vue-ui/ApiUtils/index';

const { valueKey } = API_EXTENSION_KEY;
export const getHeaderData = (dataSource:any[] = []) => {
  const data = {};
  dataSource.forEach(item => {
    if (item.name) {
      data[item.name] = item?.[valueKey] ? encodeURIComponent(item?.[valueKey]) : item?.[valueKey];
    }
  });
  return data;
};

export const itemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(i => ({ value: i, label: i }));
