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
    source?: string;// 前端自动添加
    previewFlag?: boolean;// 前端自动添加
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
    _id: string;// pane唯一标识
    name: string; // pane的tab文案
    value: string;// pane内部的组件标识
    closable?: boolean;// 是否允许关闭，true - 允许关闭，false - 禁止关闭
    forceRender?: boolean;// 被隐藏时是否渲染 DOM 结构，可选
    icon?: string;// tab文案前面的icon，可选
    active?: boolean; // 是否选中，添加不用设置，缓存时用于记录上次激活的tab pane，可选

    // 组件需要的属性
    data?: { [key: string]: any; };
};
