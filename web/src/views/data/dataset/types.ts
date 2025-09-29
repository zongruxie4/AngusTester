import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Form state for static dataset
 * Represents the data structure used when creating or updating a static dataset
 */
export type StaticDataSetFormState = {
  /** Project ID this dataset belongs to */
  projectId: string;
  /** Name of the dataset */
  name: string;
  /** Description of the dataset */
  description: string;
  /** Array of parameter name-value pairs */
  parameters: {
    /** Parameter name */
    name: string;
    /** Parameter value */
    value: string;
  }[];
  /** Dataset ID (optional for new datasets) */
  id?: string;
}

/**
 * Form state for file-based dataset
 * Represents the data structure used when creating or updating a file dataset
 */
export type FileDataSetFormState = {
  /** Project ID this dataset belongs to */
  projectId: string;
  /** Name of the dataset */
  name: string;
  /** Description of the dataset */
  description: string;
  /** Array of parameter names */
  parameters: {
    /** Parameter name */
    name: string;
  }[];
  /** File extraction configuration */
  extraction: {
    /** Source type - always 'FILE' for file datasets */
    source: 'FILE';
    /** Type of file (CSV, EXCEL, TXT) */
    fileType: ExtractionFileType;
    /** Path to the file */
    path: string;
    /** File encoding */
    encoding: string;
    /** Quote character used in the file */
    quoteChar: string;
    /** Escape character used in the file */
    escapeChar: string;
    /** Separator character used in the file */
    separatorChar: string;
    /** Row index to start reading from */
    rowIndex: string;
    /** Column index to start reading from */
    columnIndex: string;
    /** Extraction method */
    method: ExtractionMethod;
    /** Default value to use when extraction fails */
    defaultValue: string;
    /** Expression for advanced extraction methods */
    expression: string;
    /** Match item for pattern-based extraction */
    matchItem: string;
  };
  /** Dataset ID (optional for new datasets) */
  id?: string;
}

/**
 * Form state for JDBC-based dataset
 * Represents the data structure used when creating or updating a JDBC dataset
 */
export type JdbcDatasetFormState = {
  /** Project ID this dataset belongs to */
  projectId: string;
  /** Name of the dataset */
  name: string;
  /** Description of the dataset */
  description: string;
  /** Array of parameter names */
  parameters: {
    /** Parameter name */
    name: string;
  }[];
  /** JDBC extraction configuration */
  extraction: {
    /** Source type - always 'JDBC' for JDBC datasets */
    source: 'JDBC';
    /** Extraction method */
    method: ExtractionMethod;
    /** Expression for advanced extraction methods */
    expression: string;
    /** Default value to use when extraction fails */
    defaultValue: string;
    /** Match item for pattern-based extraction */
    matchItem: string;
    /** Database connection information */
    datasource: {
      /** Database type */
      type: string | undefined;
      /** Username for database connection */
      username: string;
      /** Password for database connection */
      password: string;
      /** JDBC URL for database connection */
      jdbcUrl: string;
    };
    /** SQL SELECT statement */
    select: string;
    /** Row index to start reading from */
    rowIndex: string;
    /** Column index to start reading from */
    columnIndex: string;
  };
  /** Dataset ID (optional for new datasets) */
  id?: string;
}

/**
 * Dataset item as returned from API
 * Represents a complete dataset item with all its properties
 */
export type DataSetItem = {
  /** ID of the user who created this dataset */
  createdBy: string;
  /** Name of the user who created this dataset */
  createdByName: string;
  /** Creation date in ISO format */
  createdDate: string;
  /** Description of the dataset */
  description: string;
  /** Whether the dataset has been extracted */
  extracted: boolean;
  /** Extraction configuration */
  extraction: {
    /** Default value to use when extraction fails */
    defaultValue: string;
    /** Expression for advanced extraction methods */
    expression: string;
    /** Failure message if extraction failed */
    failureMessage: string;
    /** Final extracted value */
    finalValue: string;
    /** Match item for pattern-based extraction */
    matchItem: string;
    /** Extraction method */
    method: {
      /** Method value */
      value: ExtractionMethod;
      /** Method display message */
      message: string;
    };
    /** Name of the extraction */
    name: string;
    /** Source type */
    source: ExtractionSource;
    /** Extracted value */
    value: string;
    /** File type information */
    fileType: {
      /** File type value */
      value: ExtractionFileType;
      /** File type display message */
      message: string;
    };
    /** Path to the file */
    path: string;
    /** File encoding */
    encoding: Encoding;
    /** Quote character used in the file */
    quoteChar: string;
    /** Escape character used in the file */
    escapeChar: string;
    /** Separator character used in the file */
    separatorChar: string;
    /** Row index to start reading from */
    rowIndex: string;
    /** Column index to start reading from */
    columnIndex: string;
    /** SQL SELECT statement */
    select: string;
    /** Parameter name */
    parameterName: string;
    /** Database connection information */
    datasource: {
      /** Database type */
      type: { value: string; message: string; };
      /** Username for database connection */
      username: string;
      /** Password for database connection */
      password: string;
      /** JDBC URL for database connection */
      jdbcUrl: string;
    };
  };
  /** Dataset ID */
  id: string;
  /** ID of the user who last modified this dataset */
  lastModifiedBy: string;
  /** Name of the user who last modified this dataset */
  lastModifiedByName: string;
  /** Last modification date in ISO format */
  lastModifiedDate: string;
  /** Name of the dataset */
  name: string;
  /** Array of parameter name-value pairs */
  parameters: {
    /** Parameter name */
    name: string;
    /** Parameter value */
    value: string;
  }[];
  /** Project ID this dataset belongs to */
  projectId: string;
  /** Source type */
  source?: string;
  /** Whether preview is enabled */
  preview?: boolean;
}

/**
 * Source item that uses a dataset
 * Represents a resource that references a dataset
 */
export type SourceItem = {
  /** ID of the target resource */
  targetId: string;
  /** Name of the target resource */
  targetName: string;
  /** Type of the target resource */
  targetType: {
    /** Target type value */
    value: 'API' | 'SCENARIO';
    /** Target type display message */
    message: string;
  };
  /** ID of the user who created this reference */
  createdBy: string;
  /** Name of the user who created this reference */
  createdByName: string;
  /** Creation date in ISO format */
  createdDate: string;
}

// Component props
export type DatasetListProps = {
  /** Project ID this dataset list belongs to */
  projectId: string;
  /** User information */
  userInfo: { id: string; };
  /** Application information */
  appInfo: { id: string; };
  /** Notification trigger */
  notify: string;
}

/**
 * Data source structure for preview component
 * Contains all necessary information for dataset preview
 */
export type PreviewDataSource = {
  id: string;
  projectId: string;
  extracted: boolean;
  name: string;
  extraction: {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    matchItem: string;
    method: ExtractionMethod;
    name: string;
    source: ExtractionSource;
    value: string;
    fileType: ExtractionFileType;
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
      type: string;
      username: string;
      password: string;
      jdbcUrl: string;
    };
  };
  parameters: {
    name: string;
    value: string;
  }[];
};

/**
 * Pagination configuration for preview table
 */
export type PreviewPagination = {
  current: number;
  pageSize: number;
  total: number;
  showSizeChanger: boolean;
};

/**
 * Column configuration for preview table
 */
export type PreviewColumn = {
  key: string;
  title: string;
  dataIndex: string;
  ellipsis: boolean;
};
