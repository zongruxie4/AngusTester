export interface FormType {
    name: string | undefined,
    nodeId: string | undefined,
    importType: 'ANGUS' | 'POSTMAN' | 'SWAGGER',
    file?: File | undefined,
    text?: string | undefined
  }

export interface RuleType {
    required: boolean,
    message: string,
    trigger: string
  }

export interface NodeOptionType {
    id: string,
    name: string
  }

export interface SelectedFileType {
    name: string,
    size: string
  }

export const enum ImportTypeEnum {
    SERVICE = 'PROJECT',
    SAMPLE = 'SAMPLE'
  }

export const enum MethodEnum {
    DELETE = 'DELETE',
    GET = 'GET',
    HEAD = 'HEAD',
    OPTIONS = 'OPTIONS',
    PATCH = 'PATCH',
    POST = 'POST',
    PUT = 'PUT',
    TRACE = 'TRACE'
  }

export interface ImportExistForm {
  apiIds: string[],
  name: string;
  nodeId: string | undefined;
  serviceId: string;
  serviceDomain: string;
  servicePort: string;
  importType: 'OpenAPI' | 'POSTMAN';
  file: File | undefined;
  text: string;
}

export interface ServiceOptionType {
    id: string,
    name: string,
    code: string,
    pid: string,
    auth: boolean,
    targetType: 'API' | 'PROJECT' | 'SERVICE',
    children: ServiceOptionType[],
  }

export interface ApiType {
    id: string,
    name: string,
    method: MethodEnum,
    endpoint: string,
  }
