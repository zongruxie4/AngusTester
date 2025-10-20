import qs from 'qs';
import { API_EXTENSION_KEY } from '@/utils/apis';

const valueKey = API_EXTENSION_KEY.valueKey;
const enabledKey = API_EXTENSION_KEY.enabledKey;

/**
 * API parameter item interface
 */
export interface ParamsItem {
  /** Parameter name */
  name: string;
  /** Parameter location type */
  in: 'path'|'query';
  /** Parameter description */
  description: string;
  /** Unique key for form items */
  key?: symbol;
  /** Parameter value and other properties */
  [key: string]: any;
}

/**
 * Parameter type options for select components
 */
const parameterTypeOptions = [
  {
    value: 'query',
    label: 'query'
  },
  {
    value: 'path',
    label: 'path'
  }
];

/**
 * Select option item interface
 */
export interface OptionItem {
  label: string;
  value: string;
}

/**
 * Create default parameter item with optional configuration
 * @param config - Optional configuration to override defaults
 * @returns Default parameter item
 */
export const getDefaultParams = (config = {}): ParamsItem => {
  return {
    name: '',
    in: 'query',
    description: '',
    [valueKey]: '',
    [enabledKey]: true,
    schema: {
      type: 'string'
    },
    ...config
  };
};

/**
 * Generate URI from parameters
 * @param uri - Base URI
 * @param paths - Path parameters
 * @param querys - Query parameters
 * @returns Generated URI with parameters
 */
const generateUriFromParameters = (uri: string, paths: ParamsItem[], querys?: ParamsItem[]): string => {
  if (!uri) {
    return '';
  }

  const pathUri = uri.split('?')[0] || '/';
  const originalPath = pathUri.replace(/(\S+)\?\S*/, '$1');
  let pathname = '';

  // Regular expression to match path parameters in format {name}
  const pathParameterRegex = new RegExp(/{((?!{).)*}/g);
  const decodedUrl = decodeURIComponent(originalPath);
  const uriPathParameters = decodedUrl.match(pathParameterRegex);

  if (paths?.length) {
    let tempPath = decodedUrl;
    if (paths?.length > (uriPathParameters?.length || 0)) {
      tempPath += `/{${paths?.[paths?.length - 1].name}}`;
    }
    uriPathParameters?.forEach((param, idx) => {
      tempPath = tempPath.replace(param, paths[idx]?.name ? `{${paths[idx].name}}` : '');
    });
    pathname = tempPath;
  } else {
    pathname = originalPath.replace(/{\S+}/g, '');
  }
  pathname = pathname.replace(/\/{2,}/g, '/').replace(/\/$/, '');

  // Convert query parameters to name=value format
  const queryObject = {};
  (querys || []).forEach(param => {
    queryObject[param.name] = param[valueKey];
  });
  const searchParams = qs.stringify(queryObject);

  return (pathname + '?' + searchParams?.toString()).replace(/\?$/, '');
};

/**
 * Extract parameters from URI
 * @param uri - URI to extract parameters from
 * @returns Array of parameter items
 */
const extractParametersFromUri = (uri = ''): ParamsItem[] => {
  // Extract path parameters
  const parameters: ParamsItem[] = uri.match(/{[^{}]+}/gi)?.map((item): ParamsItem => {
    return getDefaultParams({ name: item.replace(/{(\S*)}/gi, '$1'), in: 'path' });
  }) || [];

  if (/\?/.test(uri)) {
    // Extract query parameters
    new URLSearchParams(uri.replace(/\S+\?(\S*)/g, '$1')).forEach((value, key) => {
      parameters.push(getDefaultParams({ name: key, [valueKey]: value, [enabledKey]: true, in: 'query', schema: { type: 'string' } }));
    });
  }
  return parameters;
};

// Legacy export aliases for backward compatibility
export { generateUriFromParameters as getUriByParams, extractParametersFromUri as getParamsByUri, parameterTypeOptions as paramsTypeOpt };
