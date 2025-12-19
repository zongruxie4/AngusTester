import {
  EnumMessage, ExtractionMethod, ExtractionSource, HttpExtractionLocation, ExtractionFileType, DatabaseType, Encoding
} from '@xcan-angus/infra';

export type VariableDetail = {
  id: string;
  name: string;
  passwordValue: true;
  projectId: string;
  value: string;
  description: string;
  extracted: boolean;
  extraction: {
    defaultValue: string;
    expression: string;
    failureMessage: string;
    finalValue: string;
    matchItem: string;
    method: EnumMessage<ExtractionMethod>;
    name: string;
    source: EnumMessage<ExtractionSource>;
    location: EnumMessage<HttpExtractionLocation>;
    value: string;
    fileType: EnumMessage<ExtractionFileType>;
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
      type: EnumMessage<DatabaseType>;
      username: string;
      password: string;
      jdbcUrl: string;
    }
  };
  createdBy: string;
  creator: string;
  createdDate: string;
  modifiedBy: string;
  modifier: string;
  modifiedDate: string;
  preview?: boolean;
  dataSource?: { value: string; message: string; };
}
