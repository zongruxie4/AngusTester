import { HttpMethod } from '@xcan-angus/infra';

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
}
