export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';
export type AssertionCondition = 'CONTAIN' | 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IS_EMPTY' | 'IS_NULL' | 'JSON_PATH_MATCH' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'NOT_CONTAIN' | 'NOT_EMPTY' | 'NOT_EQUAL' | 'NOT_NULL' | 'REG_MATCH' | 'XPATH_MATCH'

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
            in: 'query' | 'path' | 'header' | 'cookie';
            value: string;
            enabled: boolean;
            type: 'string';
        }[];
        body: {
            format: string;
            contentEncoding: 'gzip_base64' | 'base64';
            forms: {
                name: string;
                description: string;
                enabled: string;
                type: string;
                format: string;
                contentType: string;
                contentEncoding: 'gzip_base64' | 'base64';
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
                    clientIn: 'QUERY_PARAMETER' | 'BASIC_AUTH_HEADER' | 'REQUEST_BODY';
                    username: string;
                    password: string;
                };
                password: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: 'QUERY_PARAMETER' | 'BASIC_AUTH_HEADER' | 'REQUEST_BODY';
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
        type: 'BODY' | 'BODY_SIZE' | 'DURATION';
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
            in: 'query' | 'path' | 'header' | 'cookie';
            value: string;
            enabled: boolean;
            type: 'string';
        }[];
        body: {
            format: string;
            contentEncoding: 'gzip_base64' | 'base64';
            forms: {
                name: string;
                description: string;
                enabled: string;
                type: string;
                format: string;
                contentType: string;
                contentEncoding: 'gzip_base64' | 'base64';
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
                    clientIn: 'QUERY_PARAMETER' | 'BASIC_AUTH_HEADER' | 'REQUEST_BODY';
                    username: string;
                    password: string;
                };
                password: {
                    tokenUrl: string;
                    refreshUrl: string;
                    scopes: string[];
                    clientId: string;
                    clientSecret: string;
                    clientIn: 'QUERY_PARAMETER' | 'BASIC_AUTH_HEADER' | 'REQUEST_BODY';
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
            value: 'BODY' | 'BODY_SIZE' | 'DURATION';
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
