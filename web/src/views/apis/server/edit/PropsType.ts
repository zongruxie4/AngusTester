export type ServerInfo = {
  url: string;
  description: string;
  variables: {
    [key: string]: {
      description: string;
      default: string;
      enum: string[];
    }
  };
  notEmptyContent: boolean;
  emptyContent: boolean;
  'x-xc-id': string;
}

export type ServerConfig = {
  id: string;
  serviceId?: string;
  url: string;
  description?: string;
  variables: {
    id: string;
    description: string;
    default: string;
    enum: {id:string;value:string}[];
    name: string;
  }[];
  'x-xc-id'?: string;
}
