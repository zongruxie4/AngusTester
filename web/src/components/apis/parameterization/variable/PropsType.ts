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
            value: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
            message: string;
        };
        name: string;
        source: 'FILE' | 'http' | 'HTTP_ASSERTION' | 'HTTP_SAMPLING' | 'JDBC' | 'VALUE';
        value: string;
        fileType: {
            value: 'CSV' | 'EXCEL' | 'TXT';
            message: string;
        };
        path: string;
        encoding: 'UTF-8' | 'UTF-16' | 'UTF-16BE' | 'UTF-16LE' | 'US-ASCII' | 'ISO-8859-1';
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
    showValue: string;
    description: string;
    source?: string;// 前端自动添加
    previewFlag?: boolean;// 前端自动添加
}
