/**
 * HTTP method types
 */
export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';

/**
 * Assertion condition types for test validation
 */
export type AssertionCondition = 'CONTAIN' | 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IS_EMPTY' | 'IS_NULL' | 'JSON_PATH_MATCH' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'NOT_CONTAIN' | 'NOT_EMPTY' | 'NOT_EQUAL' | 'NOT_NULL' | 'REG_MATCH' | 'XPATH_MATCH';

/**
 * HTTP configuration interface for test pipelines
 */
export type HTTPConfig = {
    target: 'HTTP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    actionOnEOF: 'RECYCLE' | 'STOP_THREAD';
    sharingMode: 'ALL_THREAD' | 'CURRENT_THREAD';
    variables: {
        name: string;
        description: string;
        // extracted:boolean;
        value: string;
        passwordValue: string;
        extraction: {
            source: 'HTTP';
            method: 'JSON_PATH' | 'REGEX' | 'X_PATH';
            expression: string;
            defaultValue: string;
            location: string;
            request: { [key: string]: any };
        };
    }[];
    datasets: {
        name: string;
        description: string;
        // extracted:boolean;
        parameters: {
            name: string;
            value: string;
        }[];
        extraction: {
            source: 'HTTP';
            method: 'JSON_PATH' | 'REGEX' | 'X_PATH';
            fileType: string;
            path: string;
            encoding: string;
            quoteChar: string;
            escapeChar: string;
            separatorChar: string;
            rowIndex: string;
            columnIndex: string;
            select: string;
            datasource: {
                type: string;
                username: string;
                password: string;
                jdbcUrl: string;
            }
        };
    }[];
    request: {
        method: HttpMethod;
        url: string;
        server: {
            url: string;
            variables?: {
                [key: string]: {
                    defaultValue: string;
                    allowableValues: string[];
                    description?: string;
                }
            };
            description?: string;
        };
        endpoint: string;
        parameters: {
            name: string;
            in: ParameterIn;
            value: string;
            enabled: boolean;
            type: 'string';
        }[];
        body: {
            format: string;
            contentEncoding: ContentEncoding;
            forms: {
                name: string;
                description: string;
                enabled: string;
                type: string;
                format: string;
                contentType: string;
                contentEncoding: ContentEncoding;
                fileName: string;
                value: string;
            }[];
            rawContent: string;
            fileName: string;
        };
        authentication: {
            type: string;
            name: string;
            enabled: boolean;
            description: string;
            value: string;
            apiKeys: {
                name: string;
                in: string;
                value: string;
            }[];
            oauth2: {
                clientCredentials: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: AuthClientIn;
                    username: string;
                    password: string;
                };
                password: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: AuthClientIn;
                    username: string;
                    password: string;
                };
                authFlow: 'clientCredentials' | 'password';
                newToken: boolean;
                token: string;
            };
        };
    };
    assertions: {
        name: string;
        enabled: boolean;
        type: BasicAssertionType;
        expected: string;
        assertionCondition: AssertionCondition;
        expression: string;
        description: string;
        parameterName: string;
        condition: string;
        extraction: {
            method: string;
            expression: string;
            matchItem: string;
            defaultValue: string;
            location: string;
            parameterName: string;
        };
    }[];
    apisId: string;
    caseId: string;
    id: string;// 前端自动生成，用于给每条记录添加id
}

/**
 * HTTP information interface with enhanced type definitions
 */
export type HTTPInfo = {
    target: 'HTTP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    actionOnEOF:{
        value:'RECYCLE' | 'STOP_THREAD';
        message:string;
    };
    sharingMode:{
        value:'ALL_THREAD' | 'CURRENT_THREAD'
        message:string;
    };
    variables: {
        name: string;
        description: string;
        // extracted:boolean;
        value: string;
        passwordValue: string;
        extraction: {
            source: 'HTTP';
            method: {
                value: 'JSON_PATH' | 'REGEX' | 'X_PATH';
                message: string;
            };
            expression: string;
            defaultValue: string;
            location: string;
            request: { [key: string]: any };
        };
    }[];
    datasets: {
        name: string;
        description: string;
        // extracted:boolean;
        parameters: {
            name: string;
            value: string;
        }[];
        extraction: {
            source: 'HTTP';
            method: {
                value: 'JSON_PATH' | 'REGEX' | 'X_PATH';
                message: string;
            };
            fileType: string;
            path: string;
            encoding: string;
            quoteChar: string;
            escapeChar: string;
            separatorChar: string;
            rowIndex: string;
            columnIndex: string;
            select: string;
            datasource: {
                type: string;
                username: string;
                password: string;
                jdbcUrl: string;
            }
        };
    }[];
    request: {
        method: HttpMethod;
        url: string;
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
        parameters: {
            name: string;
            in: ParameterIn;
            value: string;
            enabled: boolean;
            type: 'string';
        }[];
        body: {
            format: string;
            contentEncoding: ContentEncoding;
            forms: {
                name: string;
                description: string;
                enabled: string;
                type: string;
                format: string;
                contentType: string;
                contentEncoding: ContentEncoding;
                fileName: string;
                value: string;
            }[];
            rawContent: string;
        };
        authentication: {
            type: string;
            name: string;
            enabled: boolean;
            description: string;
            value: string;
            apiKeys: {
                name: string;
                in: string;
                value: string;
            }[];
            oauth2: {
                clientCredentials: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: AuthClientIn;
                    username: string;
                    password: string;
                };
                password: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: AuthClientIn;
                    username: string;
                    password: string;
                };
                authFlow: 'clientCredentials' | 'password';
                newToken: boolean;
                token: string;
            };
        };
    };
    assertions: {
        name: string;
        enabled: boolean;
        type: {
            value: BasicAssertionType;
            message: string;
        },
        expected: string;
        assertionCondition: {
            value: AssertionCondition;
            message: string;
        };
        expression: string;
        description: string;
        parameterName: string;
        condition: string;
        extraction: {
            method: {
                value: string;
                message: string;
            },
            expression: string;
            matchItem: string;
            defaultValue: string;
            location: string;
            parameterName: string;
        };
    }[];
    apisId: string;
    caseId: string;
    id: string;// 前端自动生成，用于给每条记录添加id
}

/**
 * Rendezvous configuration interface for test synchronization
 */
export type RendezvousConfig = {
  id: string; // 前端自动生成，用于给每条记录添加id
  target: 'RENDEZVOUS';
  name: string;
  description: string;
  enabled: boolean;
  beforeName: string;
  transactionName: string;
  timeoutInMs: string;
  threads: string;
}

// import { HttpMethod, AssertionCondition } from '../HTTPConfigs/PropsType';

/**
 * API information interface for test configuration
 */
export type ApiInfo = {
    projectId: string;
    caseId: string;
    apisId: string;
    id: string;
    endpoint: string;
    server?: { [key: string]: any };
    description: string;
    summary: string;
    name: string;
    method: {
        value: HttpMethod;
        message: string;
    };
    parameters: {
        name: string;
        in: string;
        description: string;
        enabled:boolean;
    }[];
    requestBody: {
        $ref:string;
        description: string;
        content: {
            [key:string]: {
                schema: {[key:string]:any};
                exampleSet: boolean;
                'x-xc-value': string;
            }
        };
        required: boolean;
    };
    authentication:{
        type: string;
        enabled: boolean;
        'x-xc-value': string;
        'x-scheme': string;
        $ref?:string;
    };
    variables:{[key:string]:any}[];
    assertions: {
        name: string;
        enabled: boolean;
        type: { value: BasicAssertionType; message: string; };
        expected: string;
        assertionCondition: { value: AssertionCondition; message: string; };
        expression: string;
        description: string;
        parameterName: string;
        condition: string;
        extraction: {
            method: { value: string; message: string; };
            expression: string;
            matchItem: string;
            defaultValue: string;
            location: string;
            parameterName: string;
        };
    }[];
    resolvedRefModels: { [key: string]: string };
    availableServers: {
        url: string;
        description?: string;
        variables?: {
            [key: string]: {
                default: string;
                description: string;
                enum: string[];
            }
        };
    }[];
}

// import { HTTPInfo, HttpMethod } from '../HTTPConfigs/PropsType';

/**
 * Use case information interface for test scenarios
 */
export type UseCaseInfo = {
    id: string;
    caseType: {
        value: string;
        message: string;
    };
    caseId: string;
    apisId: string;
    name: string;
    apisSummary: string;
    method: {
        value: HttpMethod;
        message: string;
    };
    uri: string;
    currentServer: {
        url: string;
        emptyContent: boolean;
        notEmptyContent: boolean;
        'x-xc-id': string;
        'x-xc-serverSource': string;
    };
    parameters: {
        name: string;
        in: string;
        description: string;
        schema: {
            type: string;
            exampleSet: boolean;
            types: string[];
        };
        'x-xc-enabled': boolean;
    }[];
    requestBody: {
        $ref:string;
        description: string;
        content: {
            [key:string]: {
                schema: {[key:string]:any};
                exampleSet: boolean;
                'x-xc-value': string;
            }
        };
        required: boolean;
    };
    authentication: {
        type: string;
        name: string;
        in: string;
    };
    assertions: HTTPInfo['assertions'];
    variables:{
        id:string;
        name:string;
        description:string;
        enabled:boolean;
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
        extraction:{
            method: {
                value:'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
                message:string;
            };
            expression: string;
            defaultValue: string;
            parameterName: string;
            location: 'PATH_PARAMETER'|'QUERY_PARAMETER'|'REQUEST_HEADER'|'FORM_PARAMETER'|'REQUEST_RAW_BODY'|'RESPONSE_HEADER'|'RESPONSE_BODY';
            request: {
                method:{
                    value:'POST'|'GET'|'PUT'|'PATCH'|'DELETE'|'HEAD'|'OPTIONS'|'TRACE';
                    message:string;
                };
                url:string;
                server:{
                    url:string;
                    description:string;
                    variables:{
                        allowableValues:string[];
                        defaultValue:string;
                        description:string;
                    };
                };
                endpoint:string;
                authentication:{
                    type:'none'|'http'|'apiKey'|'oauth2';
                    name:string;
                    description:string;
                    enabled:boolean;
                    value:boolean;
                    apiKeys:{
                        name:string;
                        in:string;
                        value:string;
                    }[];
                    oauth2:{
                        clientCredentials:{
                            tokenUrl:string;
                            refreshUrl:string;
                            scopes:string[];
                            clientId:string;
                            clientSecret:string;
                            clientIn:'QUERY_PARAMETER'|'BASIC_AUTH_HEADER'|'REQUEST_BODY';
                            username:string;
                            password:string;
                        };
                        password:{
                            tokenUrl:string;
                            refreshUrl:string;
                            scopes:string[];
                            clientId:string;
                            clientSecret:string;
                            clientIn:'QUERY_PARAMETER'|'BASIC_AUTH_HEADER'|'REQUEST_BODY';
                            username:string;
                            password:string;
                        };
                        authFlow:'clientCredentials'| 'password';
                        newToken: boolean;
                        token: string;
                    };
                };
                parameters:{
                    name:string;
                    in:{
                        value:'QUERY'|'PATH'|'COOKIE'|'HEADER';
                        message:string;
                    };
                    description:string;
                    enabled:boolean;
                    type:'string'|'number'|'integer'|'boolean'|'array'|'object';
                    format:string;
                    value:string;
                }[];
                body:{
                    format:string;
                    contentEncoding:'gzip_base64';
                    forms:{
                        name:string;
                        description:string;
                        enabled:boolean;
                        type:'string'|'number'|'integer'|'boolean'|'array'|'object';
                        format:string;
                        contentType:string;
                        contentEncoding:'gzip_base64';
                        fileName:string;
                        value:string;
                    }[];
                    rawContent:string;
                }
            };
        };
    }[];
}

/**
 * Throughput configuration interface for performance testing
 */
export type ThroughputConfig = {
  id:string;// 前端自动生成，用于给每条记录添加id
  target: 'THROUGHPUT';
  name: string;
  description: string;
  enabled: boolean;
  beforeName: string;
  transactionName: string;
  permitsPerSecond: string;
  timeoutInMs: string;
}

/**
 * Transaction end configuration interface
 */
export type TransEndConfig = {
  id: string;
  target: 'TRANS_END';
  name: string;
  description: string;
  beforeName: string;
  transactionName: string;
  enabled: boolean;
}

/**
 * Waiting time configuration interface for test delays
 */
export type WaitingTimeConfig = {
  id: string; // 前端自动生成，用于给每条记录添加id
  beforeName:string;
  transactionName: string;
  target: 'WAITING_TIME';
  name: string;
  description: string;
  enabled: boolean;
  maxWaitTimeInMs: string;
  minWaitTimeInMs?: string;// 固定等待时间不用传
}

/**
 * Transaction start configuration interface
 */
export type TransStartConfig = {
  id: string; // 前端自动生成，用于给每条记录添加id
  target: 'TRANS_START';
  name: string;
  description: string;
  enabled: boolean;
  beforeName: string;
  transactionName: string;
}

/**
 * Target key types for pipeline components
 */
export type TargetKey = 'HTTP' | 'TRANS_START' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 'THROUGHPUT' | 'TRANS_END';

/**
 * Pipeline configuration union type
 */
export type PipelineConfig = HTTPConfig | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig | ThroughputConfig;

/**
 * Pipeline information union type
 */
export type PipelineInfo = HTTPInfo | WaitingTimeConfig | RendezvousConfig | TransStartConfig | TransEndConfig;
