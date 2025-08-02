import { ExtractionMethod } from '@xcan-angus/infra';

export type FormState = {
    projectId: string;
    name: string;
    description: string;
    parameters: {
        name: string;
      }[];
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
