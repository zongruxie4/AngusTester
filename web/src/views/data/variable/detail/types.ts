import {
  ExtractionMethod,
  ExtractionSource,
  ExtractionFileType,
  Encoding,
  HttpExtractionLocation
} from '@xcan-angus/infra';

/**
 * Button group action types
 * Represents the different actions that can be triggered from the button group
 */
export type ButtonGroupAction = 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh' | 'cancel';

/**
 * Detail tab keys
 * Represents the different tabs available in the variable detail view
 */
export type DetailTabKey = 'value' | 'preview' | 'use';

/**
 * Component props definition
 * Defines the interface for props passed to this component
 */
export interface ButtonGroupProps {
  /**
   * Flag indicating if the variable is in edit mode
   * When true, additional action buttons are displayed
   */
  editFlag: boolean;

  /**
   * Flag indicating if the OK (save) button should be disabled
   * Typically disabled when required fields are missing
   */
  okButtonDisabled: boolean;
}

/**
 * Static variable form state
 * Defines the structure for static variable form data
 */
export type StaticVariableFormState = {
  projectId: string;
  name: string;
  value: string;
  passwordValue: boolean;
  description: string;
  id?: string;
}

/**
 * HTTP variable form state
 * Defines the structure for HTTP variable form data
 */
export type HttpVariableFormState = {
  projectId: string;
  name: string;
  description: string;
  passwordValue: false;
  extraction: {
    source: 'HTTP';
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

/**
 * JDBC variable form state
 * Defines the structure for JDBC variable form data
 */
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

/**
 * File variable form state
 * Defines the structure for file variable form data
 */
export type FileVariableFormState = {
  projectId: string;
  name: string;
  passwordValue: false,
  description: string;
  extraction: {
    source: 'FILE';
    fileType: ExtractionFileType;
    path: string;
    encoding: Encoding;
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

/**
 * Source item
 * Represents a source item where a variable is used
 */
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
