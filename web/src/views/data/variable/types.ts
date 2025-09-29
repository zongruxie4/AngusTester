import { EnumMessage, ExtractionMethod, ExtractionSource, ExtractionFileType, DatabaseType, Encoding } from '@xcan-angus/infra';

export type VariableItem = {
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
  createdByName: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  preview?: boolean;
  dataSource?: { value: string; message: string; };
}
