export type ServerVariables = {
  [key: string]: {
    default: string;
    description: string;
    enum: string[]
  };
}

export type ServerInfo = {
  'x-xc-id': string;
  description: string;
  url: string;
  variables: ServerVariables;
  extensions?: Record<string, any>;
  emptyContent: true;
  notEmptyContent: true;
}

export type ServerConfig = {
  id: string;
  'x-xc-id'?: string;
  serviceId?: string;
  url: string;
  description?: string;
  variables: {
    id: string;
    description: string;
    default: string;
    enum: { id: string; value: string }[];
    name: string;
  }[];
}
