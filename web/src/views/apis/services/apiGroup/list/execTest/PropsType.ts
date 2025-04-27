export type ServerConfig = {
    id: string;
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
