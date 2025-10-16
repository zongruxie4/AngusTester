export type Status = 'DEV_COMPLETED' | 'IN_DESIGN' | 'IN_DEV' | 'RELEASED';
export type TargetType = 'API' | 'PROJECT' | 'SERVICE';
export type Source = 'CREATED' | 'EDITOR' | 'IMPORT' | 'SYNC';
export type ImportSource = 'OPENAPI' | 'POSTMAN';

export interface IInfomation {
  apiNum: number;
  auth: boolean;
  code: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  id: string;
  importSource: { message: string; value: ImportSource; };
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  pid: string;
  source: { message: string; value: Source; };
  status: { message: string; value: Status; };
  targetType: { message: string; value: TargetType; };
}

export interface OASInfoSchema {
  serviceId: string;
  openapi: string;
  specVersion: string;
  info: {
    title: string;
    summary?: string;
    version?: string;
    description?: string;
    contact?: {
      name: string;
      url: string;
      email: string;
      extensions?: Record<string, string>;
    };
    extensions: Record<string, string>;
    license?: {
      name: string;
      url: string;
      identifier?: string;
      extensions?: Record<string, string>;
    };
    termsOfService?: string;
  };
  tags: {
    description: string;
    extensions: Record<string, string>;
    externalDocs: {
      url: string;
      description: string;
      extensions: Record<string, string>;
    };
    name: string;
  }[];
  externalDocs: {
    url: string;
    description: string;
    extensions?: Record<string, string>;
  };
  security: Record<string, string[]>[];
  servers: {
    description: string;
    extensions: Record<string, string>;
    url: string;
    variables: Record<string, {
      default: string;
      description: string;
      enum: string[];
      extensions: Record<string, string>
    }>;
  }[];
  extensions: Record<string, string>;
  lastModifiedBy: string;
  lastModifiedDate: string;
}
