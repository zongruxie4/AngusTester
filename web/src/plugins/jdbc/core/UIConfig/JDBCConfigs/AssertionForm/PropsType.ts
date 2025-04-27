export type AssertionType = 'BODY' | 'DURATION'|'BODY_SIZE'

export type AssertionCondition = 'CONTAIN' |
    'EQUAL' |
    'GREATER_THAN' |
    'GREATER_THAN_EQUAL' |
    'IS_EMPTY' |
    'IS_NULL' |
    'LESS_THAN' |
    'LESS_THAN_EQUAL' |
    'NOT_CONTAIN' |
    'NOT_EMPTY' |
    'NOT_EQUAL' |
    'NOT_NULL' |
    'REG_MATCH' |
    'XPATH_MATCH' |
    'JSON_PATH_MATCH';

export type AssertionConfig = {
    name: string;
    type: AssertionType;
    description: string;
    expected: string;
    assertionCondition: AssertionCondition;
    enabled: boolean;
    matchItem: string;
    expression: string;
}

export type AssertionInfo = {
    name: string;
    type: {value:AssertionType;message:string;};
    description: string;
    expected: string;
    assertionCondition: {value:AssertionCondition;message:string;};
    enabled: boolean;
    matchItem: string;
    expression: string;
}
