export type HttpMethod = 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS' | 'TRACE';
export type AssertionCondition = 'CONTAIN' | 'EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'IS_EMPTY' | 'IS_NULL' | 'JSON_PATH_MATCH' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'NOT_CONTAIN' | 'NOT_EMPTY' | 'NOT_EQUAL' | 'NOT_NULL' | 'REG_MATCH' | 'XPATH_MATCH'

export type HTTPConfig = {
    target: 'SMTP';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
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
            schema?: any;
        }[];
        body: { [key: string]: { [key: string]: any } };
        authentication: {
            type: string;
            enabled: boolean;
            'x-xc-value': string;
            'x-scheme': string;
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

export type SMTPInfo = {
    name: string;
    linkName?: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    assertive: boolean;
    extractableVariable: boolean;
    kind: string;
    mail: {
        content: {
            subject: string;
            suppressSubject: boolean;
            enableDebugLogging: boolean;
            message: string;
            messageSizeStatistics: boolean;
            plainBody: boolean;
            sendEmlMessage?: boolean;
        },
        mailFrom: string;
        receiverTo: string;
    }
    server: {
        connectionTimeout: string;
        password: string;
        port: number;
        readTimeout: string;
        security: {
            enforceStartTLS: boolean;
            trustAllCerts: boolean;
            use: {
                value: string;
                message: string;
            },
            useLocalTrustStore: boolean;
        };
        server: string;
        username: string;
        useAuth: boolean;
    }
    target: 'SMTP';

    id: string;// 前端自动生成，用于给每条记录添加id
}
