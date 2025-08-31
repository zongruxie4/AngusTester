import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

export type StaticDataSetFormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
    value: string;
  }[];
  id?: string;
}

export type FileDataSetFormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
  }[];
  extraction: {
    source: 'FILE';
    fileType: ExtractionFileType;
    path: string;
    encoding: string;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    method: ExtractionMethod;
    defaultValue: string;
    expression: string;
    matchItem: string;
  };
  id?: string;
}

export type JdbcDatasetFormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
  }[];
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

export type DataSetItem = {
    createdBy: string;
    createdByName: string;
    createdDate: string;
    description: string;
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
        datasource: {
            type: { value: string; message: string; };
            username: string;
            password: string;
            jdbcUrl: string;
        };
    };
    id: string;
    lastModifiedBy: string;
    lastModifiedByName: string;
    lastModifiedDate: string;
    name: string;
    parameters: {
        name: string;
        value: string;
    }[];
    projectId: string;
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
