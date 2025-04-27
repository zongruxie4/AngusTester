export type FormState = {
    projectId: string;
    name: string;
    description: string;
    passwordValue: false;
    extraction: {
        source: 'http';
        method: 'EXACT_VALUE' | 'JSON_PATH' | 'REGEX' | 'X_PATH';
        expression: string;
        defaultValue: string;
        location: 'QUERY_PARAMETER' | 'PATH_PARAMETER' | 'REQUEST_HEADER' | 'FORM_PARAMETER' | 'REQUEST_RAW_BODY' | 'RESPONSE_HEADER' | 'RESPONSE_BODY';
        matchItem: string;
        parameterName: string;
        request: { url: string; };
    };
    id?: string;
}

export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';

export type RequestConfig = {
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
    body: { [key: string]: { [key: string]: any } };
    authentication: {
        type: string;
        enabled: boolean;
        'x-xc-value': string;
        'x-scheme': string;
    };
}

export type AvailableServer = {
    url: string;
    description?: string;
    variables?: {
        [key: string]: {
            default: string;
            description: string;
            enum: string[];
        }
    };
}

export type RequestBody = {
    $ref: string;
    description: string;
    content: {
        [key: string]: {
            schema: { [key: string]: any };
            exampleSetFlag: boolean;
            'x-xc-value': string;
        }
    };
    required: boolean;
}

export type Authentication = {
    type: string;
    enabled: boolean;
    'x-xc-value': string;
    'x-scheme': string;
    $ref?: string;
}
