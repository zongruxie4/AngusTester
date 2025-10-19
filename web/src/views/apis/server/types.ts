import { OpenAPIV3_1 } from '@/types/openapi-types';

export type ServerInfo = {
  serviceId?: string;
  id?: string;
  'x-xc-id'?: string;
  url: string;
  description?: string;
  variables?: OpenAPIV3_1.ServerVariableObject[];
  extensions?: Record<string, any>;
}

export const getServerData = (dataSource: ServerInfo): string => {
  const url = dataSource.url;
  return url.replace(/\{[a-zA-Z0-9_]+\}/g, match => {
    const key = match.replace('{', '').replace('}', '');
    return dataSource.variables?.[key]?.default || '';
  });
};
