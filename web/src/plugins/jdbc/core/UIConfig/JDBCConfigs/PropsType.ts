import { AssertionConfig, AssertionInfo } from './AssertionForm/PropsType';

export type QueryType = 'SELECT' | 'UPDATE' | 'CALLABLE' | 'PREPARED_SELECT' | 'PREPARED_UPDATE'
export type InputOutputType = 'IN'| 'OUT'| 'INOUT';

export type VariableInfo = {
    id:string;
    name:string;
    description:string;
    enabled?:boolean;
    scope: {
        value: 'GLOBAL' | 'CURRENT';
        message: string;
    };
    source: {
        value: 'SCENARIO';
        message: string;
    };
    value:string;
}

export type QueryArguments = {
    type: string;
    inout: InputOutputType;
    value: string;
}

export type JDBCConfig = {
    target: 'JDBC';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    sql: string;
    assertions: AssertionConfig[];
    variables: VariableInfo[];
    timeoutInSecond: string;
    maxResultRows: string;
    type: QueryType;
    arguments: QueryArguments[];
    id: string; // 前端自动生成，用于给每条记录添加id
}

export type JDBCConfigInfo = {
    target: 'JDBC';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    sql: string;
    timeoutInSecond: string;
    maxResultRows: string;
    type: { value: QueryType; message: string; };
    arguments: QueryArguments[];
    assertions: AssertionInfo[];
    variables: VariableInfo[];
}
