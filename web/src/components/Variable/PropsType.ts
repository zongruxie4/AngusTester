export type TargetType = 'API' | 'PROJECT' | 'SCENARIO' | 'SERVICE';
export type Scope = 'CURRENT' | 'GLOBAL' | 'INHERIT';

export type EnumObj = { value: string; checked: boolean; valueErr: boolean; }
export type MethodObj = 'EXACT_VALUE' | 'REGEX' | 'JSON_PATH' | 'X_PATH';

export type VariableObj = {
  createdBy: string;
  createdByName: string;
  createdDate: string;
  description: string;
  elementName: string;
  enabled: boolean;
  extraction: {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    location: string;
    method: {value:MethodObj, message:string};
    name: string;
    parameterName: string;
    value: string;
    parameterNameErr?: boolean;
    expressionErr?: boolean;
  },
  id: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  scope: {value:Scope, message:string};
  targetId: string;
  targetName: string;
  targetType: {value:TargetType, message:string};
  value: string;
  isEdit:boolean;
  isExpand:boolean;
  isAdd:boolean;
  delLoading: boolean;
  saveLoading: boolean;
  nameErr: boolean;
  valueErr: boolean;
  type: boolean;
  enableLoading: boolean;
}

export type SaveParams = {
  name: string;
  scope: Scope;
  value: string;
  enabled: boolean;
  targetId: string;
  targetType: TargetType;
  extraction?: {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    location: string;
    method: 'EXACT_VALUE';
    name: string;
    parameterName: string;
    value: string
  };
  elementName?: string;
  description?: string;
}
