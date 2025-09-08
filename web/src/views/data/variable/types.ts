import {
  ExtractionMethod,
  ExtractionSource,
  ExtractionFileType,
  Encoding
} from '@xcan-angus/infra';

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
    dataSource?: { value: string; message: string; };
    previewFlag?: boolean;
}
