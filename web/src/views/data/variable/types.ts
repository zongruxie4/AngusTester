import {
  ExtractionMethod,
  ExtractionSource,
  ExtractionFileType,
  Encoding,
  HttpExtractionLocation
} from '@xcan-angus/infra';

export type StaticVariableFormState = {
  projectId: string;
  name: string;
  value: string;
  passwordValue: boolean;
  description: string;
  id?: string;
}

export type HttpVariableFormState = {
  projectId: string;
  name: string;
  description: string;
  passwordValue: false;
  extraction: {
    source: 'http';
    method: ExtractionMethod;
    expression: string;
    defaultValue: string;
    location: HttpExtractionLocation;
    matchItem: string;
    parameterName: string;
    request: { url: string; };
  };
  id?: string;
}

export type JdbcVariableFormState = {
  projectId: string;
  name: string;
  description: string;
  passwordValue: false;
  extraction: {
    source: 'JDBC';
    method: ExtractionMethod;
    expression: string;
    defaultValue: string;
    matchItem: string;
    datasource: {
      type: string | undefined;
      username: string;
      password: string;
      jdbcUrl: string;
    };
    select: string;
    rowIndex: string;
    columnIndex: string;
  };
  id?: string;
}

export type FileVariableFormState = {
  projectId: string;
  name: string;
  passwordValue: false,
  description: string;
  extraction: {
    source: 'FILE';
    fileType: 'CSV' | 'EXCEL' | 'TXT';
    path: string;
    encoding: string;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    defaultValue: string;
    expression: string;
    matchItem: string;
  };
  id?: string;
}

export type VariableItem = {
    createdBy: string;
    createdByName: string;
    createdDate: string;
    extracted: boolean;
    extraction: {
        defaultValue: string;
        expression: string;
        failureMessage: string;
        finalValue: string;
        matchItem: string;
        method: {
            value: ExtractionMethod;
            message: string;
        };
        name: string;
        source: ExtractionSource;
        value: string;
        fileType: {
            value: ExtractionFileType;
            message: string;
        };
        path: string;
        encoding: Encoding;
        quoteChar: string;
        escapeChar: string;
        separatorChar: string;
        rowIndex: string;
        columnIndex: string;
        select: string;
        parameterName: string;
        request: {
            url: string;
        };
        datasource: {
            type: { value: string; message: string; };
            username: string;
            password: string;
            jdbcUrl: string;
        }
    };
    id: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    passwordValue: true;
    projectId: string;
    value: string;
    description: string;
    source?: string;
    previewFlag?: boolean;
}

export type SourceItem = {
    targetId: string;
    targetName: string;
    targetType: {
        value: 'API' | 'SCENARIO';
        message: string;
    };
    createdBy: string;
    createdByName: string;
    createdDate: string;
}

export type IPane = {
    _id: string;
    name: string;
    value: string;
    closable?: boolean;
    forceRender?: boolean;
    icon?: string;
    active?: boolean;

    data?: { [key: string]: any; };
};
