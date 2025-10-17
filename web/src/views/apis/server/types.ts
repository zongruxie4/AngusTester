import { OpenAPIV3_1 } from '@/types/openapi-types';

export type ServerInfo = {
  serviceId?: string;
  id: string;
  'x-xc-id'?: string;
  url: string;
  description?: string;
  variables: OpenAPIV3_1.ServerVariableObject[];
  extensions?: Record<string, any>;
}
