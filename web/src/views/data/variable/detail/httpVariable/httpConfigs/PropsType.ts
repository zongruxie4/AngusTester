export type HttpServer = {
    id: string;
    url: string;
    variables?: any,
    description?: string;
    'x-xc-serverSource'?: string;
    '"x-xc-id"'?: string;
}

export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';
export type AssertionCondition = 'CONTAIN' | 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IS_EMPTY' | 'IS_NULL' | 'JSON_PATH_MATCH' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'NOT_CONTAIN' | 'NOT_EMPTY' | 'NOT_EQUAL' | 'NOT_NULL' | 'REG_MATCH' | 'XPATH_MATCH'

export type RequestConfigs = {
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
}
