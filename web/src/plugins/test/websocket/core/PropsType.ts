export type ScriptType = 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
export type AssertionType = 'BODY' | 'DURATION' | 'BODY_SIZE'
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

export type ParameterConfig = {
    id:string;
    name: string;
    in: 'query' | 'header';
    value: string;
    type: 'string';
    enabled: boolean;
}

export type PipelineConfig = {
    id: string;// 前端自动生成，用于给每条记录添加id
    target: 'WEBSOCKET';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    apisId: string;
    url: string;// 2000个字符
    parameters: ParameterConfig[];
    mode: 'ONLY_SEND' | 'ONLY_RECEIVE' | 'SEND_AND_RECEIVE';
    message: string;// 8192个字符
    messageEncoding: 'none' | 'base64' | 'gzip_base64';
    assertions: {
        name: string;
        type: AssertionType;
        description: string;
        expected: string;
        assertionCondition: AssertionCondition;
        enabled: boolean;
        matchItem: string;
        expression: string;
    }[];
    server: {
        url: string;
        variables: {
            [key: string]: {
                defaultValue: string;
                allowableValues: string[];
                description?: string;
            }
        };
        description?: string;
    };
    endpoint: string;
}

export type WebSocketPipelineInfo = PipelineConfig;

