export type FormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
  }[];
  extraction: {
    source: 'FILE';
    fileType: 'CSV' | 'EXCEL' | 'TXT';
    path: string;
    encoding: string;
    quoteChar: string;
    escapeChar: string;
    separatorChar: string;
    rowIndex: string;
    columnIndex: string;
    method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
    defaultValue: string;
    expression: string;
    matchItem: string;
  };
  id?: string;
}
