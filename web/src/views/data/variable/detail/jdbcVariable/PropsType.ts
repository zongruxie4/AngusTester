import { ExtractionMethod } from '@xcan-angus/infra';

export type FormState = {
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
