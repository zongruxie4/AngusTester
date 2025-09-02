import { ExtractionMethod, ExtractionSource, ExtractionFileType, Encoding } from '@xcan-angus/infra';

/**
 * Table data structure for preview table
 * Each row contains an ID and dynamic key-value pairs
 */
export type TableData = {
  [key: string]: string;
} & { id: string; };

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
