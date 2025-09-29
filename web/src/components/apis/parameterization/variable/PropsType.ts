import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Variable item interface for parameterization
 */
export type VariableItem = {
    /** Creator user ID */
    createdBy: string;
    /** Creator user name */
    createdByName: string;
    /** Creation date */
    createdDate: string;
    /** Whether the variable is extracted */
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
        /** Request configuration */
        request: {
            url: string;
        };
        /** Data source configuration */
        datasource: {
            type: { value: string; message: string; };
            username: string;
            password: string;
            jdbcUrl: string;
        }
    };
    /** Variable ID */
    id: string;
    /** Last modifier user ID */
    lastModifiedBy: string;
    /** Last modifier user name */
    lastModifiedByName: string;
    /** Last modification date */
    lastModifiedDate: string;
    /** Variable name */
    name: string;
    /** Whether the value is a password */
    passwordValue: boolean;
    /** Project ID */
    projectId: string;
    /** Variable value */
    value: string;
    /** Display value */
    showValue: string;
    /** Variable description */
    description: string;
    /** Source information (frontend auto-added) */
    source?: string;
    /** Preview flag (frontend auto-added) */
    previewFlag?: boolean;
}
