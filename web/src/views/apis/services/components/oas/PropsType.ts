import { API_EXTENSION_KEY } from '@/utils/apis';

export type ExternalDocsObj = {
  url:string;
  urlErr: {
    emptyUrl: boolean;
    errUrl: boolean;
  };
  description?: string;
  ellipsis?: boolean;
  showEllipsis?:boolean;
};

export type ComponentsType = 'schemas' | 'responses' | 'parameters' | 'examples' | 'requestBodies' | 'headers'

export type CompObj = {
  id: string;
  projectId: string;
  type: {
      value: ComponentsType;
      message: string;
  },
  key: string;
  ref: string;
  model: any;
  isQuote:boolean;
  description: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  ellipsis: boolean;
  showEllipsis: boolean;
  isEdit: boolean;
  copyLoading: boolean;
  delLoading: boolean;
  quoteName?:string;
};

export type ExampleObject = {
  summary?: string;
  description?: string;
  value?:any;
  externalValue?: string;
};

export type HeaderObject = {
  description?: string;
  schema:{
    type:string;
    format?:string;
    [API_EXTENSION_KEY.valueKey]:any
  }
};

export type SaveParams = {
  name: string;
  description?: string;
  extensions?: any;
  externalDocs?: {
    url: string;
    description?: string;
    extensions?: any;
  };
}
