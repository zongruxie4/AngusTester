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

export type SceneInfo = {
    description: string;
    favouriteFlag: boolean;
    followFlag: boolean;
    id: string;
    name: string;
    plugin: 'WebSocket';
    script: {
        plugin: 'WebSocket';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: { value: ScriptType; message: string; };
        configuration?: {
            duration: {
                unit: string;
                value: string
            };
            iterations: string;
            nodeSelectors: {
                appNodeIds: [];
                availableNodeIds: [];
                num: string;
                strategy: {
                    cpuIdleRate: string;
                    cpuSpec: string;
                    diskIdleRate: string;
                    diskSpec: string;
                    enabled: boolean;
                    idleRateEnabled: boolean;
                    lastExecuted: boolean;
                    maxTaskNum: string;
                    memoryIdleRate: string;
                    memorySpec: string;
                    specEnabled: boolean
                }
            };
            onError: {
                action: string;
                sampleError: boolean;
                sampleErrorNum: string
            };
            priority: string;
            reportInterval: {
                unit: string;
                value: string
            };
            shutdownTimeout: {
                unit: string;
                value: string
            };
            startAtDate: string;
            startMode: string;
            thread: {
                rampDownInterval: {
                    unit: string;
                    value: string
                };
                rampDownThreads: string;
                rampUpInterval: {
                    unit: string;
                    value: string
                };
                rampUpThreads: string;
                threads: string
            };
            variables: {
                description: string;
                enabled: boolean;
                name: string;
                value: string
            }[];
        };
    };
    scriptId: string;
    scriptName: string;
}

export type SceneConfig = {
    description: string;
    favouriteFlag: boolean;
    followFlag: boolean;
    id: string;
    name: string;
    plugin: 'WebSocket';
    script: {
        plugin: 'WebSocket';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: {
            duration: string;
            iterations: string;
            nodeSelectors: {
                appNodeIds: [];
                availableNodeIds: [];
                num: string;
                strategy: {
                    cpuIdleRate: string;
                    cpuSpec: string;
                    diskIdleRate: string;
                    diskSpec: string;
                    enabled: boolean;
                    idleRateEnabled: boolean;
                    lastExecuted: boolean;
                    maxTaskNum: string;
                    memoryIdleRate: string;
                    memorySpec: string;
                    specEnabled: boolean
                }
            };
            onError: {
                action: string;
                sampleError: boolean;
                sampleErrorNum: string
            };
            priority: string;
            reportInterval: {
                unit: string;
                value: string
            };
            shutdownTimeout: {
                unit: string;
                value: string
            };
            startAtDate: string;
            startMode: string;
            thread: {
                rampDownInterval: string;
                rampDownThreads: string;
                rampUpInterval: string;
                rampUpThreads: string;
                threads: string;
                resetAfterRamp: boolean;
            };
            variables: {
                description: string;
                enabled: boolean;
                name: string;
                value: string
            }[];
        };
    };
    scriptId: string;
    scriptName: string;
}

export type SaveFormData = {
    description: string;
    projectId: string;
    id: string;
    name: string;
    plugin: 'WebSocket';
    script: {
        plugin: 'WebSocket';
        task: {
            arguments: { [key: string]: any };
            pipelines: PipelineConfig[];
        };
        type: ScriptType;
        configuration: { [key: string]: any };
    };
    scriptId: string;
}
