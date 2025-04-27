
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

export type TagObj = {
  id: string;
  name: string;
  description?: string;
  externalDocs:ExternalDocsObj;
  isEdit: boolean;
  isExpand: boolean;
  isAdd: boolean;
  delLoading: boolean;
  saveLoading: boolean;
  nameErr: boolean;
  ellipsis: boolean;
  showEllipsis:boolean;
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
