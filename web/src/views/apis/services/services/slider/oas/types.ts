import { API_EXTENSION_KEY } from '@/utils/apis';

// External documentation metadata for an OAS object
export type ExternalDocsObj = {
  url: string; // external documentation URL
  urlErr: {
    emptyUrl: boolean; // true when user leaves URL empty
    errUrl: boolean; // true when URL format is invalid
  };
  description?: string; // optional description for the external docs
  ellipsis?: boolean; // UI hint: text truncated state
  showEllipsis?: boolean; // UI hint: whether to show ellipsis control
};

// All supported OAS component collections
export type ComponentsType = 'schemas' | 'responses' | 'parameters' | 'examples' | 'requestBodies' | 'headers'

// A single OAS component item (e.g., a named schema or header)
export type CompObj = {
  id: string; // unique id
  projectId: string; // owning project id
  type: {
    value: ComponentsType; // component collection
    message: string; // localized label
  };
  key: string; // component key (unique within collection)
  ref: string; // $ref path
  model: any; // raw data payload (JSON)
  isQuote: boolean; // whether the component is a reference to another
  description: string; // optional description
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  ellipsis: boolean; // UI hint flags
  showEllipsis: boolean; // UI hint flags
  isEdit: boolean; // whether current item is in edit state (UI)
  copyLoading: boolean; // UI: copy action loading
  delLoading: boolean; // UI: delete action loading
  quoteName?: string; // last referenced name chain
};

// OAS Example Object
export type ExampleObject = {
  summary?: string;
  description?: string;
  value?: any; // example payload
  externalValue?: string; // URL pointing to the example
};

// OAS Header Object (simplified for UI usage)
export type HeaderObject = {
  description?: string;
  schema: {
    type: string;
    format?: string;
    [API_EXTENSION_KEY.valueKey]: any; // vendor extension stored under dynamic key
  }
};

// Common save payload for components supporting description and external docs
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
