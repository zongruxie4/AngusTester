// Infrastructure imports
import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Dataset parameter interface
 */
interface DatasetParameter {
    name: string;
    value: string;
}

/**
 * Extraction method configuration interface
 */
interface ExtractionMethodConfig {
    value: ExtractionMethod;
    message: string;
}

/**
 * File type configuration interface
 */
interface FileTypeConfig {
    value: ExtractionFileType;
    message: string;
}

/**
 * Data source configuration interface
 */
interface DataSourceConfig {
    type: { value: string; message: string; };
    username: string;
    password: string;
    jdbcUrl: string;
}

/**
 * Extraction configuration interface
 */
interface ExtractionConfig {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    matchItem: string;
    method: ExtractionMethodConfig;
    name: string;
    source: ExtractionSource;
    value: string;
    fileType: FileTypeConfig;
    path: string;
    encoding: Encoding;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    select: string;
    parameterName: string;
    datasource: DataSourceConfig;
}

/**
 * Dataset item interface for parameterization
 */
export type DataSetItem = {
    createdBy: string;
    creator: string;
    createdDate: string;
    description: string;
    extracted: boolean;
    extraction: ExtractionConfig;
    id: string;
    modifiedBy: string;
    modifier: string;
    modifiedDate: string;
    name: string;
    parameters: DatasetParameter[];
    projectId: string;
    source?: string; // Frontend auto-added
    previewFlag?: boolean; // Frontend auto-added
}
