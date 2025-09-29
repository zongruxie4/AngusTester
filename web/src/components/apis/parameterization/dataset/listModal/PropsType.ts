import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Dataset item interface for parameterization
 */
export type DataSetItem = {
    /** Creator user ID */
    createdBy: string;
    /** Creator user name */
    createdByName: string;
    /** Creation date */
    createdDate: string;
    /** Dataset description */
    description: string;
    /** Whether the dataset is extracted */
    extracted: boolean;
    /** Extraction configuration */
    extraction: {
        /** Default value for extraction */
        defaultValue: string;
        /** Extraction expression */
        expression: string;
        /** Failure message for extraction */
        failureMessage: string;
        /** Final extracted value */
        finalValue: string;
        /** Match item for extraction */
        matchItem: string;
        /** Extraction method configuration */
        method: {
            value: ExtractionMethod;
            message: string;
        };
        /** Extraction name */
        name: string;
        /** Extraction source */
        source: ExtractionSource;
        /** Extraction value */
        value: string;
        /** File type configuration */
        fileType: {
            value: ExtractionFileType;
            message: string;
        };
        /** File path */
        path: string;
        /** File encoding */
        encoding: Encoding;
        /** Quote character */
        quoteChar: string;
        /** Escape character */
        escapeChar: string;
        /** Separator character */
        separatorChar: string;
        /** Row index */
        rowIndex: string;
        /** Column index */
        columnIndex: string;
        /** Selection criteria */
        select: string;
        /** Parameter name */
        parameterName: string;
        /** Data source configuration */
        datasource: {
            type: { value: string; message: string; };
            username: string;
            password: string;
            jdbcUrl: string;
        };
    };
    /** Dataset ID */
    id: string;
    /** Last modifier user ID */
    lastModifiedBy: string;
    /** Last modifier user name */
    lastModifiedByName: string;
    /** Last modification date */
    lastModifiedDate: string;
    /** Dataset name */
    name: string;
    /** Dataset parameters */
    parameters: {
        name: string;
        value: string;
    }[];
    /** Project ID */
    projectId: string;
    /** Source information (frontend auto-added) */
    source?: string;
    /** Preview flag (frontend auto-added) */
    previewFlag?: boolean;
}
