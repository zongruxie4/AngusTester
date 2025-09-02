// 所有的条件
export type Condition = 'EQUAL' | 'NOT_EQUAL' | 'IS_EMPTY' | 'NOT_EMPTY' | 'IS_NULL' | 'NOT_NULL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'REG_MATCH' | 'XPATH_MATCH' | 'JSON_PATH_MATCH';

// 字符串的条件
export type StringCondition = 'EQUAL' | 'NOT_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'REG_MATCH'

export type ResponseMatchInfo = {
    priority: string | undefined;
    path: {
        condition: { value: StringCondition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    body: {
        condition: { value: Condition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    parameters: {
        condition: { value: Condition; message: string; };
        expected: string | undefined;
        expression: string | undefined;
        in: 'query' | 'header';
        name: string;
    }[] | undefined;
}

export type ResponseMatchConfig = {
    priority: string | undefined;
    path: {
        condition: StringCondition;
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    body: {
        condition: Condition;
        expected: string | undefined;
        expression: string | undefined;
    } | undefined;

    parameters: {
        condition: Condition;
        expected: string | undefined;
        expression: string | undefined;
        in: 'query' | 'header';
        name: string;
    }[] | undefined;
}
