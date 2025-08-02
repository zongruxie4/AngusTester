import { ExtractionFileType, ExtractionMethod } from '@xcan-angus/infra';

export type FormState = {
  projectId: string;
  name: string;
  description: string;
  parameters: {
    name: string;
  }[];
  extraction: {
    source: 'FILE';
    fileType: ExtractionFileType;
    path: string;
    encoding: string;
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
