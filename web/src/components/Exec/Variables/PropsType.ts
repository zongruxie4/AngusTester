import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

// TODO 存在8个定义，存在重复定义
export type VariableItem = {
    createdBy: string;
    createdByName: string;
    createdDate: string;
    extracted: true;
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
        showValue: string;// 前端自动添加，用于展示预览数据
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
    source?: string;// 前端自动添加
    preview?: boolean;// 前端自动添加
}
