import { API_EXTENSION_KEY } from '@/utils/apis/index';

const { valueKey } = API_EXTENSION_KEY;

/**
 * Process header data for API requests
 * @param dataSource - Array of header parameter items
 * @returns Processed header data object
 */
export const processHeaderData = (dataSource: any[] = []) => {
  const processedData = {};
  dataSource.forEach(item => {
    if (item.name) {
      processedData[item.name] = item?.[valueKey] ? encodeURIComponent(item?.[valueKey]) : item?.[valueKey];
    }
  });
  return processedData;
};

/**
 * Header parameter item types configuration
 */
export const headerParameterItemTypes = [
  'string',
  'array',
  'boolean',
  'integer',
  'object',
  'number'
].map(itemType => ({ value: itemType, label: itemType }));

// Legacy export aliases for backward compatibility
export const getHeaderData = processHeaderData;
export const itemTypes = headerParameterItemTypes;
